# Billing Service C-Role MVP Foundations Implementation Notes

## Scope Delivered

This change moves `billing-service` from a hard-coded demo stub toward the C-role MVP foundation described in the analytics and pricing docs. The implementation now treats billing as the owner of explicit price-table assumptions, request-level cost calculation, and scope-based read models for users, teams, workflows, and price-table versions.

## Significant Changes

### 1. Versioned price-table structure

- Added an explicit billing pricing domain with `PriceTableEntry` and `PricingModel`.
- Supported the three pricing modes defined in policy:
  - `per_token`
  - `per_request`
  - `fixed`
- Seeded versioned pricing assumptions under `insightflow.billing.price-tables` in [application.yml](/Users/skax/Library/Mobile Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/services/billing-service/src/main/resources/application.yml).
- Included both an inactive historical version (`2026-02-v1`) and the active MVP version (`2026-03-v1`) to make version assumptions explicit.

### 2. Request-level cost calculation

- Added `BillingCostCalculator` to centralize billing math.
- `per_token` cost uses:
  - `prompt_tokens * unit_price_input`
  - `completion_tokens * unit_price_output`
- `per_request` and `fixed` use `unit_price_request` as the configured reference value.
- Internal cost values are normalized to 4 decimal places.
- Display totals are rounded to 2 decimal places after aggregation, matching the pricing policy guidance.
- `billable` is preserved separately from the computed amount so failed/reference requests can remain visible without conflating status and chargeability.

### 3. Seeded in-memory billing read model

- Added `BillingSeedProperties` and `BillingDataService`.
- The service now materializes `BillingRecord` values from seeded request usage plus the applicable active price-table row.
- Price-table resolution uses:
  - `service_id`
  - `model`
  - active status
  - effective date window
- Missing price-table coverage fails fast during service startup instead of silently returning bad data.

### 4. More realistic read APIs

- Preserved the existing endpoints:
  - `GET /api/billing/users/{userId}`
  - `GET /api/billing/teams/{teamId}`
  - `GET /api/billing/workflows/{workflowId}`
- Added:
  - `GET /api/billing/pricing-tables/{version}`
- Billing item responses now include request-level detail useful for analytics/UI consumers:
  - `status`
  - `billable`
  - `pricing_model`
  - `prompt_tokens`
  - `completion_tokens`
  - `total_tokens`
  - `price_table_version`
  - `occurred_at`
- Scope summaries now include `item_count` while preserving existing cost fields.
- Unknown scopes return `200` with zero totals and an empty `items` array.

## Rationale

### Why config-seeded data instead of persistence

The development guide explicitly recommends a config-file or seed-table approach for MVP price-table ownership. Because `billing-service` previously had no persistence layer or event ingestion, using configuration seeds let this change deliver a real billing foundation without inventing cross-service contracts or editing code outside service ownership.

### Why preserve existing endpoints

The existing user/team/workflow routes already matched the analytics spec examples. Keeping them stable reduces integration churn for other roles while still making the responses more realistic and internally grounded in calculated records.

### Why include fixed pricing now

`fixed` is not the first operational priority, but the pricing policy defines it as part of the supported MVP pricing model range. Implementing it now keeps the price-table model honest and avoids a future structural change just to add the enum and calculator branch.

## Test Coverage Added or Updated

- Added calculator unit coverage in [BillingCostCalculatorTest.java](/Users/skax/Library/Mobile Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/services/billing-service/src/test/java/com/insightflow/billing/service/BillingCostCalculatorTest.java)
  - `per_token`
  - `per_request`
  - `fixed`
- Added query-service unit coverage in [BillingQueryServiceTest.java](/Users/skax/Library/Mobile Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/services/billing-service/src/test/java/com/insightflow/billing/service/BillingQueryServiceTest.java)
  - user aggregation
  - team aggregation
  - price-table version reads
- Updated MockMvc coverage in [BillingServiceApplicationTest.java](/Users/skax/Library/Mobile Documents/com~apple~CloudDocs/SKALA/agile_and_msa/codex_online_shopping_example/InsightFlow/services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java)
  - enriched billing payloads
  - pricing-table endpoint
  - unknown-scope empty response behavior

## Remaining Gaps

- No `usage.tracked` event consumer exists yet.
- No persistent storage exists for price tables or billing records.
- No `cost.calculated` event publishing exists yet.
- No duplicate-event or idempotency handling exists because ingestion is not implemented yet.
- No exchange-rate version table exists.
- No recommendation or anomaly inputs are emitted from billing yet.
- No pagination/filtering exists on billing read APIs.

## Verification Notes

- Service verification was executed with the billing-service Gradle test task.
- The repository does not currently include a checked-in `gradlew` wrapper, so verification used a cached local Gradle distribution plus a temporary local Gradle home for sandbox-compatible test execution.
