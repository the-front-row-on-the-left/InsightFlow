# Notification Service C-Analytics Foundation Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move `notification-service` from a stub subscription endpoint to a test-backed internal analytics notification foundation that can model subscription preferences, ingest cost/recommendation events, and expose internal notification query results without breaking existing health/subscription behavior.

**Architecture:** Keep the public controller surface small and compatible, but split responsibilities behind it: a query service for subscription/notification reads, a dedicated event-consumer service for write-side event ingestion, and in-memory repositories/domain objects that make later Kafka or persistence adapters straightforward. The first increment stays intentionally in-memory and synchronous, but it should use event/domain boundaries that match `cost.calculated` and `optimization.recommended` contracts.

**Tech Stack:** Java 21, Spring Boot 3.4, MockMvc, JUnit 5, AssertJ

---

## Planned File Structure

**Create**

- `services/notification-service/src/main/java/com/insightflow/notification/config/NotificationContextDefaults.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationChannel.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationPreferenceStatus.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationSubscriptionPreference.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationPreferences.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/InternalNotification.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/AnalyticsNotificationEvent.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/CostCalculatedEvent.java`
- `services/notification-service/src/main/java/com/insightflow/notification/domain/OptimizationRecommendedEvent.java`
- `services/notification-service/src/main/java/com/insightflow/notification/dto/InternalNotificationResponse.java`
- `services/notification-service/src/main/java/com/insightflow/notification/repository/NotificationPreferencesRepository.java`
- `services/notification-service/src/main/java/com/insightflow/notification/repository/InternalNotificationRepository.java`
- `services/notification-service/src/main/java/com/insightflow/notification/repository/InMemoryNotificationPreferencesRepository.java`
- `services/notification-service/src/main/java/com/insightflow/notification/repository/InMemoryInternalNotificationRepository.java`
- `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationEventConsumerService.java`
- `services/notification-service/src/test/java/com/insightflow/notification/service/NotificationEventConsumerServiceTest.java`
- `services/notification-service/src/test/java/com/insightflow/notification/service/NotificationQueryServiceTest.java`

**Modify**

- `services/notification-service/src/main/java/com/insightflow/notification/controller/NotificationController.java`
- `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java`
- `services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java`

## Increment Strategy

The first real increment should remain compatible with:

- `GET /health`
- `GET /internal/notifications/subscriptions`

It should add:

- an internal read model for notification items
- an internal event-consumer service with explicit cost/recommendation event methods
- idempotent in-memory persistence for consumed notifications
- test coverage for preference filtering, duplicate-event handling, and controller/query compatibility

## Chunk 1: Query-Compatible Domain Boundary

### Task 1: Expand the subscription model behind the existing API

**Files:**

- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationChannel.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationPreferenceStatus.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationSubscriptionPreference.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/NotificationPreferences.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/NotificationPreferencesRepository.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/InMemoryNotificationPreferencesRepository.java`
- Modify: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java`
- Test: `services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java`
- Test: `services/notification-service/src/test/java/com/insightflow/notification/service/NotificationQueryServiceTest.java`

- [ ] **Step 1: Write failing tests for subscription compatibility and preference-backed query logic**

Test intent:

- existing subscription endpoint still returns `optimization.recommended` / `team_digest` / `active`
- service-level query now reads from a preference repository instead of hard-coded DTOs
- repository seed also includes a `cost.calculated` subscription so downstream analytics notifications have a real path

- [ ] **Step 2: Run the focused notification tests to verify RED**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.NotificationServiceApplicationTest --tests com.insightflow.notification.service.NotificationQueryServiceTest
```

Expected:

- subscription assertions fail or test compilation fails because the preference-backed query/domain does not exist yet

- [ ] **Step 3: Implement the minimal domain + in-memory preference repository**

Implementation notes:

- seed preferences for the default demo user/team context
- preserve current subscription ordering by keeping `optimization.recommended` first
- keep controller payload compatible by mapping domain preferences back to `NotificationSubscription`

- [ ] **Step 4: Re-run the focused tests to verify GREEN**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.NotificationServiceApplicationTest --tests com.insightflow.notification.service.NotificationQueryServiceTest
```

Expected:

- targeted tests pass

- [ ] **Step 5: Commit**

```bash
git add docs/notification-service/2026-03-20-c-analytics-notification-foundation-commit-plan.md \
  services/notification-service/src/main/java/com/insightflow/notification/domain \
  services/notification-service/src/main/java/com/insightflow/notification/repository \
  services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java \
  services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java \
  services/notification-service/src/test/java/com/insightflow/notification/service/NotificationQueryServiceTest.java
git commit -m "feat(notification): add preference-backed subscription foundation"
```

## Chunk 2: Internal Analytics Event Consumer

### Task 2: Add cost/recommendation event ingestion with idempotent notification storage

**Files:**

- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/InternalNotification.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/AnalyticsNotificationEvent.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/CostCalculatedEvent.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/OptimizationRecommendedEvent.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/InternalNotificationRepository.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/InMemoryInternalNotificationRepository.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationEventConsumerService.java`
- Test: `services/notification-service/src/test/java/com/insightflow/notification/service/NotificationEventConsumerServiceTest.java`

- [ ] **Step 1: Write failing tests for event ingestion**

Test intent:

- consuming `optimization.recommended` creates a user/team notification record
- consuming `cost.calculated` creates an analytics cost notification record
- muted preferences block notification creation
- duplicate event delivery is ignored for idempotency

- [ ] **Step 2: Run the focused event-consumer tests to verify RED**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.service.NotificationEventConsumerServiceTest
```

Expected:

- test compilation fails or assertions fail because the event-consumer + internal repository do not exist yet

- [ ] **Step 3: Implement the minimal event-consumer path**

Implementation notes:

- use explicit records for `CostCalculatedEvent` and `OptimizationRecommendedEvent`
- store notifications in-memory using a deterministic dedupe key derived from event type, request id, channel, and scope
- resolve active preferences before persisting notifications
- keep write-side logic out of `NotificationQueryService`

- [ ] **Step 4: Re-run the focused event-consumer tests to verify GREEN**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.service.NotificationEventConsumerServiceTest
```

Expected:

- targeted tests pass

- [ ] **Step 5: Commit**

```bash
git add services/notification-service/src/main/java/com/insightflow/notification/domain \
  services/notification-service/src/main/java/com/insightflow/notification/repository \
  services/notification-service/src/main/java/com/insightflow/notification/service/NotificationEventConsumerService.java \
  services/notification-service/src/test/java/com/insightflow/notification/service/NotificationEventConsumerServiceTest.java
git commit -m "feat(notification): consume analytics cost and recommendation events"
```

## Chunk 3: Internal Query Surface for Notifications

### Task 3: Expose internal notification items while keeping existing endpoints intact

**Files:**

- Create: `services/notification-service/src/main/java/com/insightflow/notification/config/NotificationContextDefaults.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/dto/InternalNotificationResponse.java`
- Modify: `services/notification-service/src/main/java/com/insightflow/notification/controller/NotificationController.java`
- Modify: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java`
- Modify: `services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java`

- [ ] **Step 1: Write failing integration tests for internal notification querying**

Test intent:

- `GET /internal/notifications` returns consumed notification items in a stable order
- `GET /internal/notifications/subscriptions` remains compatible
- `GET /health` stays unchanged

- [ ] **Step 2: Run the integration tests to verify RED**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.NotificationServiceApplicationTest
```

Expected:

- `/internal/notifications` fails because the endpoint/query mapping is missing

- [ ] **Step 3: Implement the controller/query mapping**

Implementation notes:

- use default demo `user_id` and `team_id` values from configuration when query params are absent
- return DTOs dedicated to API output rather than exposing domain objects directly
- keep read concerns in `NotificationQueryService`

- [ ] **Step 4: Re-run the integration tests to verify GREEN**

Run:

```bash
./gradlew :services:notification-service:test --tests com.insightflow.notification.NotificationServiceApplicationTest
```

Expected:

- integration tests pass

- [ ] **Step 5: Commit**

```bash
git add services/notification-service/src/main/java/com/insightflow/notification/config/NotificationContextDefaults.java \
  services/notification-service/src/main/java/com/insightflow/notification/controller/NotificationController.java \
  services/notification-service/src/main/java/com/insightflow/notification/dto/InternalNotificationResponse.java \
  services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java \
  services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java
git commit -m "feat(notification): expose internal analytics notifications"
```

## Chunk 4: Documentation and Integration Notes

### Task 4: Record implementation details for the main-branch integrator

**Files:**

- Create: `docs/notification-service/2026-03-20-c-analytics-notification-foundation-notes.md`

- [ ] **Step 1: Document significant design choices**

Document:

- why this increment stays in-memory
- how preferences and notifications are separated
- how idempotency works
- what would be needed to replace the consumer with a Kafka listener

- [ ] **Step 2: Document remaining gaps**

Document:

- no external broker/listener yet
- no persistent store yet
- no notification delivery channel implementation yet
- recommendation/cost message templates are intentionally simple

- [ ] **Step 3: Commit**

```bash
git add docs/notification-service/2026-03-20-c-analytics-notification-foundation-notes.md
git commit -m "docs(notification): record analytics notification foundation notes"
```

## Suggested Integrator Commit Breakdown

1. `feat(notification): add preference-backed subscription foundation`
2. `feat(notification): consume analytics cost and recommendation events`
3. `feat(notification): expose internal analytics notifications`
4. `docs(notification): record analytics notification foundation notes`
