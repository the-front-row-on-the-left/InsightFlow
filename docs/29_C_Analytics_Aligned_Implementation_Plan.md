# C Analytics Aligned Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align `usage-service`, `billing-service`, `recommendation-service`, `notification-service`, and `apps/ai-platform-ui` with the agreed service definitions so Kafka-backed analytics data flows into a visible AIOps UI.

**Architecture:** `usage-service` consumes `ai.requested` and `ai.completed`, stores normalized request usage in its own database, then emits `usage.tracked`. `billing-service` consumes that event, stores cost records in its own database, and emits `cost.calculated`. `recommendation-service` reads `usage` and `billing` APIs to build savings recommendations on demand and does not publish Kafka events in this phase. `notification-service` stores alert records for `limit.exceeded` and cost anomalies. `gateway-service` exposes AIOps BFF endpoints, and `apps/ai-platform-ui` renders that surface by calling Gateway only.

**Tech Stack:** Spring Boot, PostgreSQL, Kafka, FastAPI, Vue 3, Vite, Docker Compose, Flyway, JUnit, pytest, Vitest

---

## Chunk 1: Usage Runtime Foundation

### Task 1: Define usage persistence and event contract foundations

**Files:**
- Create: `services/usage-service/src/main/resources/db/migration/V1__create_usage_tables.sql`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/UsageEventSnapshot.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/TrackedUsageEvent.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/AiRequestedEvent.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/AiCompletedEvent.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/repository/UsageEventSnapshotRepository.java`
- Modify: `services/usage-service/src/main/resources/application.yml`
- Modify: `services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java`

- [ ] **Step 1: Write the failing persistence/bootstrap tests**

```java
@Test
void contextStartsWithUsageFlywayAndJdbcRepositories() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:usage-service:test --tests com.insightflow.usage.UsageServiceApplicationTest`
Expected: FAIL because flyway/jdbc config and new repositories do not exist

- [ ] **Step 3: Write minimal implementation**

Add Flyway/JDBC dependencies if missing, usage DB config, migration file, and repository/domain types for request snapshots and tracked usage records.

- [ ] **Step 4: Run test to verify it passes**

Run: same command as Step 2
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/usage-service/src/main/resources/application.yml services/usage-service/src/main/resources/db/migration/V1__create_usage_tables.sql services/usage-service/src/main/java/com/insightflow/usage/domain services/usage-service/src/main/java/com/insightflow/usage/repository services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java
git commit -m "feat(usage-service): add usage persistence foundation"
```

### Task 2: Implement usage Kafka consume/merge/publish pipeline

**Files:**
- Create: `services/usage-service/src/main/java/com/insightflow/usage/config/UsageKafkaProperties.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageEventMergeService.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageTrackedEventPublisher.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageKafkaConsumer.java`
- Create: `services/usage-service/src/test/java/com/insightflow/usage/service/UsageEventMergeServiceTest.java`
- Create: `services/usage-service/src/test/java/com/insightflow/usage/service/UsageKafkaConsumerTest.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java`

- [ ] **Step 1: Write the failing event merge test**

```java
@Test
void merges_ai_requested_and_ai_completed_into_tracked_usage() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:usage-service:test --tests com.insightflow.usage.service.UsageEventMergeServiceTest`
Expected: FAIL because merge service does not exist

- [ ] **Step 3: Write minimal implementation**

Consume `ai.requested` and `ai.completed`, store snapshots keyed by `event_id`, upsert request state by `request_id`, emit `usage.tracked` only when completion data is present, and persist tracked usage once.

- [ ] **Step 4: Run focused tests**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:usage-service:test --tests com.insightflow.usage.service.UsageEventMergeServiceTest --tests com.insightflow.usage.service.UsageKafkaConsumerTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/usage-service/src/main/java/com/insightflow/usage/config services/usage-service/src/main/java/com/insightflow/usage/service services/usage-service/src/test/java/com/insightflow/usage/service
git commit -m "feat(usage-service): consume execution events and publish usage"
```

### Task 3: Back usage query APIs with persisted data

**Files:**
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java`
- Modify: `services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java`

- [ ] **Step 1: Write failing controller tests for persisted reads**

```java
@Test
void returns_user_usage_from_persisted_tracked_records() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:usage-service:test --tests com.insightflow.usage.UsageServiceApplicationTest`
Expected: FAIL because query path still uses in-memory seeds

- [ ] **Step 3: Write minimal implementation**

Swap query service to read persisted `usage_records` and keep response contract stable.

- [ ] **Step 4: Run usage suite**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:usage-service:test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java services/usage-service/src/test/java/com/insightflow/usage
git commit -m "feat(usage-service): serve persisted usage analytics"
```

## Chunk 2: Billing Runtime Foundation

### Task 4: Define billing persistence and price table storage

**Files:**
- Create: `services/billing-service/src/main/resources/db/migration/V1__create_billing_tables.sql`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/domain/CalculatedCostEvent.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/repository/BillingRecordRepository.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/repository/PricingTableRepository.java`
- Modify: `services/billing-service/src/main/resources/application.yml`
- Modify: `services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java`

- [ ] **Step 1: Write the failing billing bootstrap test**

```java
@Test
void contextStartsWithBillingFlywayAndPriceTableStorage() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:billing-service:test --tests com.insightflow.billing.BillingServiceApplicationTest`
Expected: FAIL because billing DB persistence is not configured

- [ ] **Step 3: Write minimal implementation**

Add billing DB config, Flyway migration, repositories, and persistent price table bootstrap.

- [ ] **Step 4: Run focused tests**

Run: same command as Step 2
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/billing-service/src/main/resources/application.yml services/billing-service/src/main/resources/db/migration/V1__create_billing_tables.sql services/billing-service/src/main/java/com/insightflow/billing/domain services/billing-service/src/main/java/com/insightflow/billing/repository services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java
git commit -m "feat(billing-service): add billing persistence foundation"
```

### Task 5: Implement billing usage consumer and cost publisher

**Files:**
- Create: `services/billing-service/src/main/java/com/insightflow/billing/config/BillingKafkaProperties.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/service/UsageTrackedConsumer.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/service/CostCalculatedPublisher.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingCostCalculator.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingDataService.java`
- Create: `services/billing-service/src/test/java/com/insightflow/billing/service/UsageTrackedConsumerTest.java`

- [ ] **Step 1: Write the failing consumer test**

```java
@Test
void consumes_usage_tracked_and_persists_cost_record_once() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:billing-service:test --tests com.insightflow.billing.service.UsageTrackedConsumerTest`
Expected: FAIL because no consumer/publisher exists

- [ ] **Step 3: Write minimal implementation**

Consume `usage.tracked`, calculate cost from persisted price table, persist one billing record per request, publish `cost.calculated`, and ignore duplicate `event_id`.

- [ ] **Step 4: Run focused tests**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:billing-service:test --tests com.insightflow.billing.service.UsageTrackedConsumerTest --tests com.insightflow.billing.service.BillingCostCalculatorTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/config services/billing-service/src/main/java/com/insightflow/billing/service services/billing-service/src/test/java/com/insightflow/billing/service
git commit -m "feat(billing-service): calculate and publish request costs"
```

### Task 6: Back billing queries with persisted records

**Files:**
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/controller/BillingController.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingQueryService.java`
- Modify: `services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java`

- [ ] **Step 1: Write failing persisted billing query tests**

```java
@Test
void returns_user_billing_from_persisted_cost_records() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:billing-service:test --tests com.insightflow.billing.BillingServiceApplicationTest`
Expected: FAIL because query path still depends on seeded in-memory data

- [ ] **Step 3: Write minimal implementation**

Read from persisted `billing_records`, expose pricing table reads from storage, and keep response contract stable for UI consumers.

- [ ] **Step 4: Run billing suite**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:billing-service:test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/controller/BillingController.java services/billing-service/src/main/java/com/insightflow/billing/service/BillingQueryService.java services/billing-service/src/test/java/com/insightflow/billing
git commit -m "feat(billing-service): serve persisted billing analytics"
```

## Chunk 3: Recommendation and Notification Alignment

### Task 7: Integrate recommendation-service with usage and billing APIs

**Files:**
- Create: `services/recommendation-service/app/clients/usage_client.py`
- Create: `services/recommendation-service/app/clients/billing_client.py`
- Modify: `services/recommendation-service/app/core/config.py`
- Modify: `services/recommendation-service/app/services/recommendation_engine.py`
- Modify: `services/recommendation-service/app/services/snapshot_repository.py`
- Create: `services/recommendation-service/tests/test_clients.py`
- Modify: `services/recommendation-service/tests/test_recommendation_engine.py`

- [ ] **Step 1: Write the failing integration test**

```python
def test_build_recommendations_uses_usage_and_billing_snapshots(): ...
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD/services/recommendation-service":/app -w /app python:3.12-slim sh -lc "pip install --no-cache-dir -r requirements.txt >/tmp/pip.log && pytest tests/test_recommendation_engine.py tests/test_clients.py -q"`
Expected: FAIL because API-backed client integration is missing

- [ ] **Step 3: Write minimal implementation**

Load usage and billing snapshots via HTTP clients, normalize them for the rule engine, keep a fallback stub mode for local tests only if explicitly configured, and return recommendations from the API without Kafka publication in this phase.

- [ ] **Step 4: Run recommendation suite**

Run: `docker run --rm -v "$PWD/services/recommendation-service":/app -w /app python:3.12-slim sh -lc "pip install --no-cache-dir -r requirements.txt >/tmp/pip.log && pytest -q"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/recommendation-service/app services/recommendation-service/tests
git commit -m "feat(recommendation-service): integrate usage and billing analytics"
```

### Task 8: Rework notification-service around limit and anomaly alerts

**Files:**
- Create: `services/notification-service/src/main/resources/db/migration/V1__create_notification_tables.sql`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/domain/LimitExceededEvent.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/NotificationRecordRepository.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/repository/NotificationPreferenceJdbcRepository.java`
- Create: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationKafkaConsumer.java`
- Modify: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationEventConsumerService.java`
- Modify: `services/notification-service/src/main/java/com/insightflow/notification/service/NotificationQueryService.java`
- Modify: `services/notification-service/src/main/resources/application.yml`
- Modify: `services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java`
- Create: `services/notification-service/src/test/java/com/insightflow/notification/service/NotificationKafkaConsumerTest.java`

- [ ] **Step 1: Write the failing alert persistence test**

```java
@Test
void stores_limit_exceeded_and_cost_alert_notifications() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:notification-service:test --tests com.insightflow.notification.NotificationServiceApplicationTest`
Expected: FAIL because DB-backed alert storage and Kafka consumption are missing

- [ ] **Step 3: Write minimal implementation**

Persist notification preferences and notifications, consume `limit.exceeded` and `cost.calculated`, classify alert severity using the agreed MVP thresholds, and expose alert-focused query results.

- [ ] **Step 4: Run notification suite**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:notification-service:test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/notification-service/src/main/resources/application.yml services/notification-service/src/main/resources/db/migration/V1__create_notification_tables.sql services/notification-service/src/main/java/com/insightflow/notification services/notification-service/src/test/java/com/insightflow/notification
git commit -m "feat(notification-service): store and serve ops alerts"
```

## Chunk 4: Gateway, AIOps UI, and Runtime Integration

### Task 9: Add Gateway AIOps BFF endpoints

**Files:**
- Modify: `services/gateway-service/src/main/java/com/insightflow/gateway/GatewayContractStubController.java`
- Create: `services/gateway-service/src/main/java/com/insightflow/gateway/AIOpsGatewayClient.java`
- Create: `services/gateway-service/src/test/java/com/insightflow/gateway/GatewayAIOpsContractTest.java`

- [ ] **Step 1: Write the failing gateway contract test**

```java
@Test
void proxies_aiops_usage_billing_recommendation_and_notification_reads() {}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:gateway-service:test --tests com.insightflow.gateway.GatewayAIOpsContractTest`
Expected: FAIL because AIOps BFF routes do not exist

- [ ] **Step 3: Write minimal implementation**

Expose pass-through BFF endpoints:
- `GET /api/aiops/usage/users/{user_id}`
- `GET /api/aiops/billing/users/{user_id}`
- `GET /api/aiops/recommendations?user_id=...`
- `GET /api/aiops/notifications?user_id=...`

- [ ] **Step 4: Run gateway tests**

Run: `docker run --rm -v "$PWD":/workspace -w /workspace gradle:8.10.2-jdk21-alpine gradle --no-daemon :services:gateway-service:test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/gateway-service/src/main/java/com/insightflow/gateway services/gateway-service/src/test/java/com/insightflow/gateway
git commit -m "feat(gateway-service): add aiops analytics bff"
```

### Task 10: Add AIOps analytics views to ai-platform-ui

**Files:**
- Create: `apps/ai-platform-ui/src/features/aiops/api.ts`
- Create: `apps/ai-platform-ui/src/features/aiops/types.ts`
- Create: `apps/ai-platform-ui/src/features/aiops/components/UsagePanel.vue`
- Create: `apps/ai-platform-ui/src/features/aiops/components/BillingPanel.vue`
- Create: `apps/ai-platform-ui/src/features/aiops/components/RecommendationPanel.vue`
- Create: `apps/ai-platform-ui/src/features/aiops/components/NotificationPanel.vue`
- Modify: `apps/ai-platform-ui/src/App.vue`
- Modify: `apps/ai-platform-ui/src/lib/api.ts`
- Create: `apps/ai-platform-ui/src/features/aiops/__tests__/AppAIOps.test.ts`

- [ ] **Step 1: Write the failing UI test**

```ts
it('renders aiops panels fed by usage billing recommendation and notification queries', async () => {})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd apps/ai-platform-ui && npm test -- --run`
Expected: FAIL because AIOps panels do not exist

- [ ] **Step 3: Write minimal implementation**

Add a simple AIOps dashboard section that fetches one user’s usage, billing, recommendations, and notifications through Gateway and renders them in separate panels.

- [ ] **Step 4: Run frontend tests and build**

Run: `cd apps/ai-platform-ui && npm test -- --run && npm run build`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add apps/ai-platform-ui/src apps/ai-platform-ui/package.json apps/ai-platform-ui/vitest.config.* apps/ai-platform-ui/package-lock.json
git commit -m "feat(ai-platform-ui): add aiops analytics dashboard"
```

### Task 11: Update compose and end-to-end runtime wiring

**Files:**
- Modify: `compose.yaml`
- Modify: `README.md`
- Modify: `.env.example`
- Modify: `services/usage-service/src/main/resources/application.yml`
- Modify: `services/billing-service/src/main/resources/application.yml`
- Modify: `services/notification-service/src/main/resources/application.yml`
- Modify: `services/recommendation-service/app/core/config.py`

- [ ] **Step 1: Write the failing runtime verification checklist in docs**

```md
- usage-service talks to usage_db and Kafka
- billing-service talks to billing_db and Kafka
- notification-service talks to notification_db and Kafka
- recommendation-service can reach usage/billing APIs
- gateway-service can reach all four analytics services
- ai-platform-ui can reach Gateway AIOps APIs
```

- [ ] **Step 2: Run compose config to verify current gap**

Run: `docker compose config`
Expected: config lacks separate analytics DBs and aligned env wiring

- [ ] **Step 3: Write minimal implementation**

Add one Postgres container with three logical analytics databases, wire service-specific URLs/env vars, update shared event topics, and document the run path.

- [ ] **Step 4: Run compose verification**

Run: `docker compose config`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add compose.yaml README.md .env.example services/usage-service/src/main/resources/application.yml services/billing-service/src/main/resources/application.yml services/notification-service/src/main/resources/application.yml services/recommendation-service/app/core/config.py
git commit -m "build: wire analytics runtime integration"
```

### Task 12: Verify end-to-end analytics flow

**Files:**
- Create: `docs/30_C_Analytics_Runtime_Verification.md`

- [ ] **Step 1: Define verification scenario**

```md
1. Publish ai.requested
2. Publish ai.completed
3. Confirm usage row and usage API
4. Confirm billing row and billing API
5. Confirm recommendation API
6. Publish limit.exceeded
7. Confirm notification API and Gateway BFF
8. Confirm AIOps UI
```

- [ ] **Step 2: Run end-to-end verification commands**

Run:
- `docker compose up --build -d`
- publish sample Kafka events
- query service APIs
- run frontend build/tests

Expected: All services respond with aligned data for the same request/user/team

- [ ] **Step 3: Capture verification results**

Record actual commands and outputs in `docs/30_C_Analytics_Runtime_Verification.md`.

- [ ] **Step 4: Commit**

```bash
git add docs/30_C_Analytics_Runtime_Verification.md
git commit -m "docs: capture analytics runtime verification"
```

---

Plan complete and saved to `docs/29_C_Analytics_Aligned_Implementation_Plan.md`. Execution will proceed in this session with subagent-driven development.
