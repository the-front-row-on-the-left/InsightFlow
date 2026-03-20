# Usage Service C-Role MVP Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the hard-coded usage stub with a request-centric MVP foundation that stores realistic seeded usage records in-memory, aggregates them by scope, and serves stable read APIs for user/team/service analytics.

**Architecture:** Keep the existing HTTP endpoints and response envelope, but move the data source behind a repository boundary. Introduce small domain records for request usage facts and query scope, then let `UsageQueryService` translate repository data into API DTOs with date filtering and summary metrics.

**Tech Stack:** Java 21, Spring Boot 3.4, MockMvc, JUnit 5, AssertJ

---

## Chunk 1: Query Contract First

### Task 1: Add failing tests for realistic scope reads

**Files:**
- Modify: `services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java`
- Create: `services/usage-service/src/test/java/com/insightflow/usage/service/UsageQueryServiceTest.java`
- Touch later: `services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java`
- Touch later: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java`

- [x] **Step 1: Write the failing HTTP contract assertions**

Add coverage for:
- existing user/team/service endpoints returning repository-backed totals
- optional `from`, `to`, and `unit` query parameters
- richer item/summary fields that reflect request-level usage state

- [x] **Step 2: Run the focused controller test suite and confirm failure**

Run: `gradle :services:usage-service:test --tests com.insightflow.usage.UsageServiceApplicationTest`

Expected:
- assertions fail because the current stub ignores date filters and does not expose richer usage details

- [x] **Step 3: Write the failing service-level aggregation tests**

Cover:
- aggregation by `user`, `team`, and `service`
- average token/latency calculation from seeded request rows
- empty-scope handling returning `200`-compatible zero summaries

- [x] **Step 4: Run the focused service test suite and confirm failure**

Run: `gradle :services:usage-service:test --tests com.insightflow.usage.service.UsageQueryServiceTest`

Expected:
- compilation or assertion failure because the repository-backed query boundary does not exist yet

- [x] **Step 5: Commit**

Completed in working tree; no commit created in this task handoff.

```bash
git add services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java services/usage-service/src/test/java/com/insightflow/usage/service/UsageQueryServiceTest.java
git commit -m "test: define usage-service analytics query expectations"
```

## Chunk 2: Repository-Backed Usage Foundations

### Task 2: Implement request-centric domain and seeded persistence

**Files:**
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/UsageRecord.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/UsageQuery.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/domain/UsageScopeType.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/repository/UsageRecordRepository.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/repository/InMemoryUsageRecordRepository.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java`
- Note: `services/usage-service/src/main/resources/application.yml` ended up unchanged for this slice

- [x] **Step 1: Write the minimal repository/domain model to satisfy the service tests**

Model the MVP request facts from the analytics docs:
- request identity and scope fields
- model/status/policy/limit outcomes
- prompt/completion/total token usage
- latency plus request timestamp for range filtering

- [x] **Step 2: Seed realistic usage rows in the in-memory repository**

Seed multiple requests spanning:
- repeated users and teams
- at least two services
- success, failed, and blocked request states
- different dates so range filtering has observable behavior

- [x] **Step 3: Update `UsageQueryService` to aggregate from the repository**

Implement:
- scope filtering
- date range filtering
- summary math
- DTO mapping for items and summaries

- [x] **Step 4: Run service tests and make them green**

Run: `gradle :services:usage-service:test --tests com.insightflow.usage.service.UsageQueryServiceTest`

Expected:
- PASS

- [x] **Step 5: Commit**

Completed in working tree; no commit created in this task handoff.

```bash
git add services/usage-service/src/main/java/com/insightflow/usage/domain/UsageRecord.java services/usage-service/src/main/java/com/insightflow/usage/domain/UsageQuery.java services/usage-service/src/main/java/com/insightflow/usage/domain/UsageScopeType.java services/usage-service/src/main/java/com/insightflow/usage/repository/UsageRecordRepository.java services/usage-service/src/main/java/com/insightflow/usage/repository/InMemoryUsageRecordRepository.java services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java services/usage-service/src/main/resources/application.yml
git commit -m "feat: add usage-service request repository foundation"
```

### Task 3: Preserve API compatibility while upgrading response fidelity

**Files:**
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/dto/UsageItem.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/dto/UsageSummary.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/dto/UsageScopeResponse.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/dto/UsagePeriod.java`
- Touch maybe: `services/usage-service/src/main/java/com/insightflow/usage/dto/UsageStatusResponse.java`

- [x] **Step 1: Extend DTOs only where it keeps backward compatibility**

Keep existing field names and endpoints intact, but add response data that makes the API usable for analytics MVP:
- workflow id
- prompt/completion token counts
- policy / limit outcomes
- success / failed / blocked summary counts

- [x] **Step 2: Accept optional query parameters in the controller**

Support:
- `from`
- `to`
- `unit`

Defaults:
- derive from the repository result window when parameters are absent

- [x] **Step 3: Run the HTTP contract tests and make them green**

Run: `gradle :services:usage-service:test --tests com.insightflow.usage.UsageServiceApplicationTest`

Expected:
- PASS

- [x] **Step 4: Commit**

Completed in working tree; no commit created in this task handoff.

```bash
git add services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java services/usage-service/src/main/java/com/insightflow/usage/dto/UsageItem.java services/usage-service/src/main/java/com/insightflow/usage/dto/UsageSummary.java services/usage-service/src/main/java/com/insightflow/usage/dto/UsageScopeResponse.java services/usage-service/src/main/java/com/insightflow/usage/dto/UsagePeriod.java services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java
git commit -m "feat: serve repository-backed usage analytics responses"
```

## Chunk 3: Verification And Service Notes

### Task 4: Verify end-to-end service tests and document implementation

**Files:**
- Modify: `docs/usage-service/2026-03-20-usage-service-c-role-commit-plan.md`
- Create: `docs/usage-service/2026-03-20-usage-service-c-role-implementation-notes.md`

- [x] **Step 1: Run the full usage-service test suite**

Run: `gradle :services:usage-service:test`

Expected:
- PASS

- [x] **Step 2: Update the plan checkboxes to reflect what shipped**

Record any deviations from the original sequence.

- [x] **Step 3: Write the implementation notes / change log**

Document:
- significant code changes
- rationale for the in-memory repository choice
- assumptions made from analytics specs
- known gaps such as event consumers, durable persistence, and `usage.tracked` publishing

- [x] **Step 4: Commit**

Completed in working tree; no commit created in this task handoff.

## Execution Notes

- Verification used local `javac` compilation plus a temporary JUnit launcher because the workspace has no `gradlew` wrapper and `gradle` is not installed in the sandbox.
- `UsageScopeResponse`, `UsagePeriod`, and `application.yml` stayed unchanged because the current slice only needed richer items/summaries plus query parameter support in the controller.
- Controller parameter annotations were made explicit to avoid relying on compiler `-parameters` metadata during MVC binding.

```bash
git add docs/usage-service/2026-03-20-usage-service-c-role-commit-plan.md docs/usage-service/2026-03-20-usage-service-c-role-implementation-notes.md
git commit -m "docs: capture usage-service c-role implementation notes"
```
