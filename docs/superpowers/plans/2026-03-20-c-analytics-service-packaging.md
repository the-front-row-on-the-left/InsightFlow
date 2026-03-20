# C Analytics Service Packaging Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the C-owned backend services into package-structured Spring apps while preserving their current API contracts.

**Architecture:** Keep the shared common-web module intact, but move each C service from a single-file controller/application style into per-service controller, dto, and service packages. Preserve behavior with MockMvc tests first, then move response-building logic into service-layer classes.

**Tech Stack:** Spring Boot, Gradle multi-project build, MockMvc tests, Docker-based Gradle verification

---

### Task 1: Lock current C service contracts with tests

**Files:**
- Create: `services/notification-service/src/test/java/com/insightflow/notification/NotificationServiceApplicationTest.java`
- Modify: existing C service tests if package names need adjustment

- [ ] Step 1: Write the failing notification-service MockMvc tests for health and subscription endpoints.
- [ ] Step 2: Run the C-service tests and verify at least the new notification test fails before implementation.

### Task 2: Refactor usage-service into packages

**Files:**
- Create: `services/usage-service/src/main/java/com/insightflow/usage/controller/UsageController.java`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/dto/...`
- Create: `services/usage-service/src/main/java/com/insightflow/usage/service/UsageQueryService.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/UsageServiceApplication.java`

- [ ] Step 1: Move endpoint wiring into a controller class.
- [ ] Step 2: Move response records into dto package.
- [ ] Step 3: Move stub data creation into service layer.
- [ ] Step 4: Run usage-service tests and confirm they pass.

### Task 3: Refactor billing-service, recommendation-service, and notification-service into packages

**Files:**
- Create: service-specific `controller`, `dto`, and `service` classes under each C service
- Modify: `BillingServiceApplication.java`, `RecommendationServiceApplication.java`, `NotificationServiceApplication.java`

- [ ] Step 1: Apply the same split to billing-service.
- [ ] Step 2: Apply the same split to recommendation-service.
- [ ] Step 3: Apply the same split to notification-service.
- [ ] Step 4: Run the targeted C-service test suite and confirm it passes.
