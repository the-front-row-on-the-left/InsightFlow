# MSA Service Containers And Analytics Skeleton Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make each backend service runnable as an independent container and align analytics query skeleton APIs with the owner C documents.

**Architecture:** Keep the existing Gradle multi-project build at the repository root, but produce one bootable Spring Boot jar per service and build each service image from a shared Dockerfile with per-service build arguments. Extend the usage, billing, and recommendation services with document-based read-only API skeletons so the existing frontend can integrate later without adding a second analytics UI now.

**Tech Stack:** Gradle multi-project Spring Boot services, Docker multi-stage builds, Docker Compose, MockMvc endpoint tests

---

## Chunk 1: Analytics API Skeleton

### Task 1: Lock the usage API contract with tests

**Files:**
- Create: `services/usage-service/src/test/java/com/insightflow/usage/UsageServiceApplicationTest.java`
- Modify: `services/usage-service/src/main/java/com/insightflow/usage/UsageServiceApplication.java`

- [ ] Step 1: Write failing MockMvc tests for user/team/service usage endpoints.
- [ ] Step 2: Run the usage-service tests and confirm they fail because endpoints or response fields are missing.
- [ ] Step 3: Implement the minimal controller response shape with period and summary fields.
- [ ] Step 4: Run the usage-service tests again and confirm they pass.

### Task 2: Lock the billing and recommendation API contract with tests

**Files:**
- Create: `services/billing-service/src/test/java/com/insightflow/billing/BillingServiceApplicationTest.java`
- Create: `services/recommendation-service/src/test/java/com/insightflow/recommendation/RecommendationServiceApplicationTest.java`
- Modify: `services/billing-service/src/main/java/com/insightflow/billing/BillingServiceApplication.java`
- Modify: `services/recommendation-service/src/main/java/com/insightflow/recommendation/RecommendationServiceApplication.java`

- [ ] Step 1: Write failing tests for billing user/team/workflow queries and recommendation user query.
- [ ] Step 2: Run those tests and verify the failure matches the missing contract.
- [ ] Step 3: Implement minimal read-only skeleton payloads based on docs 15, 20, and 24.
- [ ] Step 4: Re-run the tests and confirm they pass.

## Chunk 2: Independent Service Containers

### Task 3: Make service artifacts image-friendly

**Files:**
- Modify: `build.gradle`
- Create: `.dockerignore`
- Create: `Dockerfile`

- [ ] Step 1: Add a failing expectation by checking the current repository lacks a reusable service image path.
- [ ] Step 2: Update Gradle service projects to emit a stable boot jar name per service.
- [ ] Step 3: Add a shared multi-stage Dockerfile that builds one selected service jar and runs it.
- [ ] Step 4: Verify the Dockerfile paths and Gradle task names line up with the project layout.

### Task 4: Define app containers in Compose

**Files:**
- Modify: `compose.yaml`
- Modify: `.env.example`

- [ ] Step 1: Add compose service definitions for gateway, policy, rate-limit, usage, billing, notification, and recommendation.
- [ ] Step 2: Wire each service to the right ports and container-network environment variables.
- [ ] Step 3: Keep infrastructure containers intact and make gateway use service DNS names instead of localhost for upstream calls.
- [ ] Step 4: Review the resulting compose file for independent service startup flow.

## Chunk 3: Documentation And Verification

### Task 5: Update the local run guide

**Files:**
- Modify: `README.md`

- [ ] Step 1: Replace the stale build guidance with the Gradle and Docker Compose workflow.
- [ ] Step 2: Document that analytics APIs now follow the owner C skeleton endpoints and that no separate analytics frontend is added yet.

### Task 6: Verify what is actually runnable in this workspace

**Files:**
- No file edits.

- [ ] Step 1: Run the available verification commands for tests or build tooling.
- [ ] Step 2: Record any blocked verification caused by missing Gradle wrapper/local Gradle.
- [ ] Step 3: Summarize the final state with evidence, including any remaining gap the user should know about.
