# Billing Service C-Role MVP Foundations Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the billing-service stub with a test-backed MVP read model that owns versioned price tables, calculates request costs from explicit pricing assumptions, and serves realistic billing read APIs while preserving the existing user/team/workflow endpoints.

**Architecture:** Keep the current Spring Boot HTTP surface, but move the response generation onto a small in-memory billing domain aligned with the analytics and pricing docs. Seed versioned price-table entries and sample request usage data from billing-service configuration, calculate request-level costs through a dedicated calculator using BigDecimal rounding rules, then aggregate those calculated records for scope-based read APIs and a new pricing-table read API.

**Tech Stack:** Java 21, Spring Boot 3.4, Jackson records, BigDecimal cost math, MockMvc, JUnit 5, AssertJ

---

## File Structure

### Existing files to modify

- `services/billing-service/src/main/java/com/insightflow/billing/BillingServiceApplication.java`
  - Enable configuration property scanning for billing seed data.
- `services/billing-service/src/main/java/com/insightflow/billing/controller/BillingController.java`
  - Preserve current endpoints and add a price-table read endpoint.
- `services/billing-service/src/main/java/com/insightflow/billing/service/BillingQueryService.java`
  - Replace the hard-coded response stub with aggregation over calculated records.
- `services/billing-service/src/main/java/com/insightflow/billing/dto/BillingItem.java`
  - Expand request-level billing output with realistic fields while keeping existing cost fields.
- `services/billing-service/src/main/java/com/insightflow/billing/dto/BillingSummary.java`
  - Add summary metadata needed by scope reads without breaking existing total fields.
- `services/billing-service/src/main/resources/application.yml`
  - Define explicit versioned pricing assumptions and sample request usage seeds.
- `services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java`
  - Update controller assertions to match calculated billing responses and the new pricing endpoint.

### New files to create

- `services/billing-service/src/main/java/com/insightflow/billing/config/BillingSeedProperties.java`
  - Configuration properties for price-table seeds and request usage seeds.
- `services/billing-service/src/main/java/com/insightflow/billing/domain/BillingRecord.java`
  - Calculated request-level billing record used for aggregation and API mapping.
- `services/billing-service/src/main/java/com/insightflow/billing/domain/BillingRequestUsage.java`
  - Raw request usage input before cost calculation.
- `services/billing-service/src/main/java/com/insightflow/billing/domain/PriceTableEntry.java`
  - Versioned billing price-table record aligned with the pricing policy.
- `services/billing-service/src/main/java/com/insightflow/billing/domain/PricingModel.java`
  - Enum for `per_token`, `per_request`, and `fixed`.
- `services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableEntryResponse.java`
  - API DTO for one price-table row.
- `services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableResponse.java`
  - API DTO for a price-table version read.
- `services/billing-service/src/main/java/com/insightflow/billing/service/BillingCostCalculator.java`
  - Dedicated calculator that applies pricing-model rules and rounding policy.
- `services/billing-service/src/main/java/com/insightflow/billing/service/BillingDataService.java`
  - Loads seeded data, resolves applicable price-table entries, and materializes billing records.
- `services/billing-service/src/test/java/com/insightflow/billing/service/BillingCostCalculatorTest.java`
  - Unit tests for pricing-model calculations and rounding behavior.
- `services/billing-service/src/test/java/com/insightflow/billing/service/BillingQueryServiceTest.java`
  - Unit tests for scope filtering, aggregation, and price-table lookup behavior.

## Chunk 1: Pricing Model and Seeded Domain

### Task 1: Write the calculator tests first

**Files:**
- Create: `services/billing-service/src/test/java/com/insightflow/billing/service/BillingCostCalculatorTest.java`
- Create later: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingCostCalculator.java`
- Create later: `services/billing-service/src/main/java/com/insightflow/billing/domain/PricingModel.java`
- Create later: `services/billing-service/src/main/java/com/insightflow/billing/domain/PriceTableEntry.java`
- Create later: `services/billing-service/src/main/java/com/insightflow/billing/domain/BillingRequestUsage.java`
- Create later: `services/billing-service/src/main/java/com/insightflow/billing/domain/BillingRecord.java`

- [ ] **Step 1: Write the failing test for per-token cost calculation**

```java
@Test
void calculatesPerTokenCostUsingInputAndOutputUnitPrices() {
    // prompt 900 * 0.08 + completion 320 * 0.32 = 174.4000
}
```

- [ ] **Step 2: Run the focused test to verify RED**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingCostCalculatorTest`
Expected: FAIL because calculator/domain classes do not exist yet

- [ ] **Step 3: Add the minimal pricing domain and calculator implementation**

Implementation outline:

```java
public enum PricingModel {
    PER_TOKEN,
    PER_REQUEST,
    FIXED
}
```

```java
BigDecimal costBeforeRounding = switch (priceTableEntry.pricingModel()) {
    case PER_TOKEN -> inputCost.add(outputCost);
    case PER_REQUEST -> priceTableEntry.unitPriceRequest();
    case FIXED -> priceTableEntry.unitPriceRequest();
};
```

- [ ] **Step 4: Add a second failing test for per-request cost and a third for fixed reference pricing**

```java
@Test
void calculatesPerRequestCostFromRequestUnitPrice() {}

@Test
void treatsFixedPricingAsConfiguredReferenceCost() {}
```

- [ ] **Step 5: Run the focused test class again to verify GREEN**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingCostCalculatorTest`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/domain \
        services/billing-service/src/main/java/com/insightflow/billing/service/BillingCostCalculator.java \
        services/billing-service/src/test/java/com/insightflow/billing/service/BillingCostCalculatorTest.java
git commit -m "feat(billing-service): add pricing calculator foundations"
```

### Task 2: Bind seeded pricing tables and request usage

**Files:**
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/BillingServiceApplication.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/config/BillingSeedProperties.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingDataService.java`
- Modify: `services/billing-service/src/main/resources/application.yml`

- [ ] **Step 1: Write the failing query-service test that expects seeded calculated records**

```java
@Test
void returnsVersionedPriceTableEntriesForRequestedVersion() {}
```

- [ ] **Step 2: Run the focused query-service test to verify RED**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingQueryServiceTest`
Expected: FAIL because seeded data loader and query service behavior are not implemented

- [ ] **Step 3: Implement property binding and seeded data materialization**

Implementation outline:

```java
@ConfigurationProperties(prefix = "insightflow.billing")
public record BillingSeedProperties(
        List<PriceTableSeed> priceTables,
        List<RequestUsageSeed> requests
) {}
```

```yaml
insightflow:
  billing:
    price-tables:
      - price-table-version: 2026-03-v1
        service-id: svc_doc_summary
        model: gpt-4o-mini
        pricing-model: per_token
```

- [ ] **Step 4: Materialize `BillingRecord` values by resolving the applicable active price-table entry for each seeded request**

Expected behavior:
- resolve by `service_id`, `model`, active status, and effective date window
- throw a clear exception if a seeded request has no matching price table
- keep `price_table_version`, `currency`, `billable`, and `cost_before_rounding` on each record

- [ ] **Step 5: Re-run the focused query-service test class**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingQueryServiceTest`
Expected: at least the price-table lookup assertions PASS, with remaining aggregation tests still RED until the query mapping is added

- [ ] **Step 6: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/BillingServiceApplication.java \
        services/billing-service/src/main/java/com/insightflow/billing/config/BillingSeedProperties.java \
        services/billing-service/src/main/java/com/insightflow/billing/service/BillingDataService.java \
        services/billing-service/src/main/resources/application.yml
git commit -m "feat(billing-service): seed versioned billing data"
```

## Chunk 2: Scope Read APIs and Aggregation

### Task 3: Replace hard-coded query responses with calculated aggregates

**Files:**
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/service/BillingQueryService.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/dto/BillingItem.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/dto/BillingSummary.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableEntryResponse.java`
- Create: `services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableResponse.java`
- Create: `services/billing-service/src/test/java/com/insightflow/billing/service/BillingQueryServiceTest.java`

- [ ] **Step 1: Write the failing unit tests for user/team/workflow aggregation**

```java
@Test
void aggregatesUserBillingFromCalculatedRequestRecords() {}

@Test
void aggregatesTeamBillingAfterSummingUnroundedCosts() {}

@Test
void returnsPriceTableVersionDetails() {}
```

- [ ] **Step 2: Run the query-service test class and verify RED**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingQueryServiceTest`
Expected: FAIL because `BillingQueryService` still returns hard-coded values

- [ ] **Step 3: Implement aggregation and DTO mapping**

Implementation outline:

```java
BigDecimal totalBeforeRounding = records.stream()
        .map(BillingRecord::costBeforeRounding)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

String totalCost = totalBeforeRounding.setScale(2, RoundingMode.HALF_UP).toPlainString();
```

API mapping expectations:
- preserve `scope_type`, `scope_id`, `currency`, `price_table_version`, `summary`, and `items`
- add realistic request-level fields such as `status`, `billable`, `pricing_model`, `prompt_tokens`, and `completion_tokens`
- return `200` with empty `items` and zero totals for unknown scopes

- [ ] **Step 4: Re-run the query-service tests**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.service.BillingQueryServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/service/BillingQueryService.java \
        services/billing-service/src/main/java/com/insightflow/billing/dto/BillingItem.java \
        services/billing-service/src/main/java/com/insightflow/billing/dto/BillingSummary.java \
        services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableEntryResponse.java \
        services/billing-service/src/main/java/com/insightflow/billing/dto/PricingTableResponse.java \
        services/billing-service/src/test/java/com/insightflow/billing/service/BillingQueryServiceTest.java
git commit -m "feat(billing-service): expose calculated billing read models"
```

### Task 4: Update HTTP endpoints and integration coverage

**Files:**
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/controller/BillingController.java`
- Modify: `services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java`

- [ ] **Step 1: Write failing MockMvc assertions for enriched billing responses and pricing-table reads**

```java
@Test
void returnsPricingTableByVersion() throws Exception {}

@Test
void returnsZeroTotalsForUnknownUserScope() throws Exception {}
```

- [ ] **Step 2: Run the integration test class to verify RED**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.BillingServiceApplicationTest`
Expected: FAIL because the controller does not yet expose pricing-table reads and unknown-scope behavior is not asserted

- [ ] **Step 3: Implement the controller updates**

Endpoint additions:
- `GET /api/billing/pricing-tables/{version}`

Endpoint expectations:
- existing user/team/workflow endpoints keep their paths
- pricing-table endpoint returns version metadata plus explicit pricing rows
- health endpoint stays unchanged

- [ ] **Step 4: Re-run the integration test class**

Run: `./gradlew :services:billing-service:test --tests com.insightflow.billing.BillingServiceApplicationTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add services/billing-service/src/main/java/com/insightflow/billing/controller/BillingController.java \
        services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java
git commit -m "feat(billing-service): add pricing table API coverage"
```

## Chunk 3: Final Verification and Documentation

### Task 5: Service-level verification

**Files:**
- No additional production files

- [ ] **Step 1: Run the full billing-service test suite**

Run: `./gradlew :services:billing-service:test`
Expected: PASS with all billing-service tests green

- [ ] **Step 2: Review the implementation against the requested scope**

Checklist:
- explicit versioned price-table structure exists
- cost calculation logic covers `per_token`, `per_request`, and `fixed`
- query APIs read from calculated request records instead of hard-coded stubs
- tests cover calculator logic and HTTP/API behavior
- no files outside `services/billing-service/**` and `docs/billing-service/**` were edited

### Task 6: Write implementation notes

**Files:**
- Create: `docs/billing-service/2026-03-20-c-role-mvp-foundations-implementation-notes.md`

- [ ] **Step 1: Document significant changes**

Include:
- price-table structure and versioning approach
- seeded request usage assumptions
- cost-calculation rules and rounding decisions
- API compatibility notes
- tests added and what they cover

- [ ] **Step 2: Document remaining gaps**

Include:
- no external event ingestion yet
- no persistent storage yet
- no `cost.calculated` event publishing yet
- no exchange-rate table support yet

- [ ] **Step 3: Commit**

```bash
git add docs/billing-service/2026-03-20-c-role-mvp-foundations-implementation-notes.md
git commit -m "docs(billing-service): capture c-role mvp implementation notes"
```

## Suggested Commit Breakdown

1. `feat(billing-service): add pricing calculator foundations`
2. `feat(billing-service): seed versioned billing data`
3. `feat(billing-service): expose calculated billing read models`
4. `feat(billing-service): add pricing table API coverage`
5. `docs(billing-service): capture c-role mvp implementation notes`
