# Usage Service C-Role MVP Implementation Notes

## Summary

This slice moves `usage-service` from a hard-coded stub into a request-centric MVP foundation aligned with the analytics docs. The service now reads from a seeded in-memory repository, aggregates request usage by user/team/service scope, supports optional date-window query parameters, and returns richer usage details without changing the existing endpoint URLs.

## Significant Changes

### 1. Request-domain and persistence foundation

Added new domain and repository types under `services/usage-service/src/main/java/com/insightflow/usage/`:

- `domain/UsageRecord.java`
- `domain/UsageQuery.java`
- `domain/UsageScopeType.java`
- `repository/UsageRecordRepository.java`
- `repository/InMemoryUsageRecordRepository.java`

What this gives us:

- a request-centric record model with `request_id`, user/team/service/workflow scope, model, status, policy/limit outcomes, token counts, latency, and request timestamp
- a clear repository boundary so the query service no longer owns raw sample data
- seed data covering multiple users, teams, services, dates, and statuses (`SUCCEEDED`, `FAILED`, `BLOCKED`)

Rationale:

- The analytics spec and C-role guide emphasize request-level usage storage and merge-friendly status data before durable storage is introduced.
- In-memory seeded persistence is the smallest useful step that lets the read APIs behave like real analytics queries without taking on schema, migrations, and event-consumer plumbing in the same pass.

### 2. Query service and controller behavior

Updated:

- `service/UsageQueryService.java`
- `controller/UsageController.java`

Behavior changes:

- existing endpoints remain the same:
  - `GET /api/usage/users/{userId}`
  - `GET /api/usage/teams/{teamId}`
  - `GET /api/usage/services/{serviceId}`
- endpoints now accept optional `from`, `to`, and `unit` query parameters
- summaries are calculated from repository data instead of hard-coded constants
- item lists are sorted by request timestamp descending
- empty scopes now return zero-value summaries with an empty `items` list instead of requiring special-case stub responses

Implementation details:

- date windows default to the seeded repository’s min/max request dates
- if `to < from`, the service normalizes by swapping the bounds
- controller annotations explicitly name `@PathVariable` and `@RequestParam` values so binding does not depend on compiler `-parameters`

### 3. DTO fidelity upgrades

Updated:

- `dto/UsageItem.java`
- `dto/UsageSummary.java`

New response data added while preserving existing endpoint shape:

- `workflow_id`
- `policy_result`
- `limit_result`
- `prompt_tokens`
- `completion_tokens`
- `requested_at`
- `succeeded_requests`
- `failed_requests`
- `blocked_requests`

Rationale:

- These fields make the response usable for analytics MVP consumers and closer to the `usage.tracked` / usage-log shape described in the specs.
- `UsageScopeResponse` and `UsagePeriod` did not need to change for this slice.

### 4. Test coverage and verification

Updated or added:

- `src/test/java/com/insightflow/usage/service/UsageQueryServiceTest.java`
- `src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java`
- `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`

Coverage now includes:

- user/team/service aggregation from seeded records
- date-window filtering
- zero-result scope handling
- HTTP contract assertions for richer summaries/items
- MVC slice wiring for the controller, service, and in-memory repository

Verification performed:

- compiled usage-service + shared common-web sources with `javac -proc:none`
- ran JUnit tests through a temporary local launcher against:
  - `com.insightflow.usage.service.UsageQueryServiceTest`
  - `com.insightflow.usage.UsageServiceApplicationTest`
- final result: `7 tests successful, 0 tests failed`

Why the Mockito extension was added:

- the sandbox/JDK environment could not self-attach the inline Byte Buddy mock maker during Spring test startup
- `mock-maker-subclass` avoids that runtime-only failure and keeps service-local tests runnable in constrained environments

## Assumptions Taken From Analytics Docs

- request-level usage is the correct MVP center of gravity
- policy and limit outcomes belong with usage queryable data even before event-merge consumers exist
- blocked requests can legitimately exist with `0` tokens and still matter for analytics summaries
- integer average fields are acceptable for the current API contract
- preserving the current URLs is more important than introducing a new request-detail endpoint in this pass

## Remaining Gaps

- No Kafka consumer yet for `ai.requested`, `policy.checked`, or `limit.applied`
- No request-state merge pipeline yet to build `usage.tracked`
- No `usage.tracked` publisher yet for downstream billing/recommendation work
- No durable storage or schema/migration work yet; repository data is still seeded in memory
- No daily/weekly/monthly materialized summary tables yet
- No request-detail read endpoint yet
- No observability counters/metrics yet for duplicate events, delayed merges, or price-table gaps

## Recommended Next Increment

1. Add event payload/domain types for the three upstream events plus an internal merge service keyed by `request_id`.
2. Replace the seed repository with a persistence abstraction that can support upsert/idempotency.
3. Emit a normalized `usage.tracked` shape once a request becomes merge-complete or terminal.
