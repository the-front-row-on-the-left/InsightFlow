# Notification Service Analytics Foundation Notes

## Summary

This increment moves `notification-service` from a controller + hard-coded DTO stub to a small internal analytics notification foundation with separate read/write responsibilities.

What is now in place:

- preference-backed subscription queries
- explicit internal notification domain objects
- a write-side consumer service for `cost.calculated` and `optimization.recommended`
- idempotent in-memory notification storage
- an internal notification query endpoint in addition to the existing subscription endpoint

The existing compatibility points were kept:

- `GET /health` still returns the same payload
- `GET /internal/notifications/subscriptions` still returns the original `optimization.recommended` / `team_digest` / `active` entry first

## Significant Changes

### 1. Subscription data moved behind domain/repository boundaries

Previously `NotificationQueryService` returned a single hard-coded DTO list.

Now the service reads from:

- `NotificationPreferencesRepository`
- `NotificationPreferences`
- `NotificationSubscriptionPreference`

This keeps the public API stable while giving the service a real place to model:

- channel choice
- preference status
- future per-user/team overrides

The seeded in-memory preferences intentionally include:

1. `optimization.recommended` -> `team_digest`
2. `cost.calculated` -> `team_digest`
3. `optimization.recommended` -> `user_inbox`

That combination preserves current compatibility and also gives the new event consumer a user-targeted path for recommendation events.

### 2. Read-side and write-side responsibilities are separated

`NotificationQueryService` is now strictly the read boundary:

- map preference domain objects to `NotificationSubscription`
- map internal notification domain objects to `InternalNotificationResponse`
- resolve default demo context when request params are omitted

`NotificationEventConsumerService` is the write boundary:

- accept analytics event records
- resolve active preferences
- decide recipient scope from channel
- persist deduplicated `InternalNotification` projections

This split keeps controller logic thin and leaves a clean place for a future Kafka listener or broker adapter to call into without mixing read concerns into the consumer path.

### 3. Internal notifications are queryable

A new internal endpoint was added:

- `GET /internal/notifications`

This returns the stored notification projection for the resolved user/team context and is ordered by `occurred_at` descending. The endpoint is intentionally internal-facing and does not try to represent external delivery state yet.

### 4. Idempotency is handled in the in-memory repository

`InMemoryInternalNotificationRepository` stores notifications by a deterministic key:

- `event_type`
- `request_id`
- `channel`
- recipient type/id

That means duplicate delivery of the same event for the same audience is ignored. This is a small but meaningful foundation for the at-least-once event handling style described in the analytics docs.

## Rationale

### Why stay in-memory for now

The goal of this branch is to make notification-service real enough for C-role analytics work without overcommitting to persistence or broker wiring before the surrounding contracts fully settle.

An in-memory repository is enough to prove:

- event-to-notification transformation shape
- preference filtering
- idempotency behavior
- query/controller contract behavior

It also keeps the service easy to refactor once a shared broker/persistence choice lands.

### Why explicit event records instead of generic maps

`CostCalculatedEvent` and `OptimizationRecommendedEvent` give the service an internal contract that is close to the documented analytics vocabulary while still being simple to evolve.

That should make the next step straightforward:

- a Kafka listener or message adapter can deserialize broker payloads
- then delegate to `NotificationEventConsumerService.consume(...)`

## Tests and Verification

Implemented test coverage:

- `NotificationQueryServiceTest`
  - seeded subscription mapping
  - notification query ordering
- `NotificationEventConsumerServiceTest`
  - cost event routing
  - optimization event routing
  - duplicate delivery handling
  - muted preference filtering
- `NotificationServiceApplicationTest`
  - health endpoint compatibility
  - subscription endpoint compatibility
  - internal notification endpoint behavior

Verification completed in this environment:

- direct `javac` compilation of all notification-service main sources against locally cached dependencies: success
- direct `javac` compilation of all notification-service test sources against locally cached dependencies: success
- manual JUnit execution of the pure unit tests (`NotificationQueryServiceTest`, `NotificationEventConsumerServiceTest`): 6/6 tests passed

Verification gap:

- full Spring Boot integration execution could not be cleanly validated with the repository’s normal workflow because this checkout does not include `./gradlew` and no `gradle` binary is installed
- a manual integration-test runtime attempt failed before app startup because the global cached classpath pulled in conflicting SLF4J versions, which is an environment/classpath issue rather than a notification-service code failure

## Remaining Gaps

- no Kafka listener or broker adapter yet
- no persistent store yet
- no delivery execution layer for email, inbox, or digest publishing
- recommendation/cost message formatting is intentionally simple
- no explicit metrics/logging around consumed notifications yet
- no user-managed preference write API yet

## Suggested Next Increment

The cleanest next step would be:

1. add a transport adapter that converts broker payloads into `CostCalculatedEvent` / `OptimizationRecommendedEvent`
2. keep delegating into `NotificationEventConsumerService`
3. replace the in-memory repositories with a durable store once the data retention shape is agreed
