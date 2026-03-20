<template>
  <main class="app-shell">
    <section class="hero">
      <p class="eyebrow">InsightFlow</p>
      <h1>AI Ops Platform Basic Package</h1>
      <p class="description">
        문서 패키지를 기준으로 만든 최소 UI 골격입니다. 다음 단계는 Gateway 실행 API와
        Usage/Billing/Recommendation 조회 API를 연결하는 것입니다.
      </p>
    </section>

    <section class="card-grid">
      <article v-for="service in services" :key="service.service_id" class="service-card">
        <span class="badge">{{ service.category }}</span>
        <h2>{{ service.name }}</h2>
        <p>{{ service.pricing_model }}</p>
        <small>{{ service.supported_models.join(', ') }}</small>
      </article>
    </section>
  </main>
</template>

<script setup lang="ts">
const services = [
  {
    service_id: 'svc_doc_summary',
    name: 'Document Summary',
    category: 'analysis',
    pricing_model: 'per_token',
    supported_models: ['gpt-4o-mini'],
  },
  {
    service_id: 'svc_team_policy',
    name: 'Team Policy Check',
    category: 'governance',
    pricing_model: 'internal',
    supported_models: ['policy-service'],
  },
  {
    service_id: 'svc_cost_review',
    name: 'Cost Recommendation',
    category: 'analytics',
    pricing_model: 'per_request',
    supported_models: ['rule-based'],
  },
  {
    service_id: 'svc_rate_limit_guard',
    name: 'Rate Limit Guard',
    category: 'control',
    pricing_model: 'internal',
    supported_models: ['rate-limit-service'],
  },
]
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family: "Helvetica Neue", Arial, sans-serif;
  background: #f5f1e8;
  color: #1f1f1f;
}

:global(*) {
  box-sizing: border-box;
}

.app-shell {
  min-height: 100vh;
  padding: 48px 24px 64px;
  background:
    radial-gradient(circle at top left, rgba(212, 167, 98, 0.35), transparent 28%),
    linear-gradient(180deg, #f8f4ec 0%, #efe5d6 100%);
}

.hero {
  max-width: 760px;
  margin: 0 auto 32px;
}

.eyebrow {
  margin: 0 0 12px;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  font-size: clamp(36px, 6vw, 64px);
  line-height: 0.98;
}

.description {
  max-width: 620px;
  margin-top: 18px;
  font-size: 18px;
  line-height: 1.6;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  max-width: 1100px;
  margin: 0 auto;
}

.service-card {
  padding: 20px;
  border: 1px solid rgba(31, 31, 31, 0.08);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
}

.badge {
  display: inline-block;
  margin-bottom: 12px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #1f1f1f;
  color: #f8f4ec;
  font-size: 12px;
  text-transform: uppercase;
}

h2 {
  margin: 0 0 8px;
  font-size: 20px;
}

p,
small {
  display: block;
  margin: 0;
  line-height: 1.5;
}

small {
  margin-top: 12px;
  color: #5d564d;
}
</style>
