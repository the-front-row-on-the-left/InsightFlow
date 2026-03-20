<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">Recommendations</h1>
        <p class="page-subtitle">토큰 절감과 비용 최적화를 위한 추천 목록입니다.</p>
      </div>
      <div class="toolbar">
        <div class="field">
          <label>User ID</label>
          <input v-model="userId" type="text" />
        </div>
      </div>
    </div>

    <section class="panel">
      <p class="panel-title">Optimization Recommendations</p>
      <div v-if="response && response.recommendations.length > 0" class="recommendation-list">
        <article v-for="item in response.recommendations" :key="`${item.service_id}-${item.recommended_model}`" class="recommendation-card">
          <div class="recommendation-head">
            <div>
              <h2>{{ item.service_id }}</h2>
              <p>{{ item.current_model }} → {{ item.recommended_model }}</p>
            </div>
            <span :class="confidenceClass(item.confidence)">{{ item.confidence }}</span>
          </div>
          <div class="recommendation-metrics">
            <div>
              <span>Estimated Savings</span>
              <strong>{{ item.estimated_monthly_savings }}</strong>
            </div>
            <div>
              <span>Token Reduction</span>
              <strong>{{ item.estimated_token_savings.toLocaleString() }}</strong>
            </div>
          </div>
          <p class="reason">{{ item.reason }}</p>
        </article>
      </div>
      <div v-else class="empty-state">현재 표시할 추천이 없습니다.</div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { getRecommendations } from '../platform/dashboardApi'
import type { RecommendationResponse } from '../platform/types'

const userId = ref('u_demo_001')
const response = ref<RecommendationResponse | null>(null)

function confidenceClass(confidence: string) {
  if (confidence === 'high') return 'pill pill-success'
  if (confidence === 'medium') return 'pill pill-warning'
  return 'pill pill-muted'
}

async function loadRecommendations() {
  response.value = await getRecommendations(userId.value)
}

onMounted(loadRecommendations)
watch(userId, loadRecommendations)
</script>

<style scoped>
.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.recommendation-card {
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}

.recommendation-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.recommendation-head h2 {
  font-size: 16px;
  color: #0f172a;
  font-weight: 800;
}

.recommendation-head p {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.recommendation-metrics {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 16px;
}

.recommendation-metrics div {
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.recommendation-metrics span {
  display: block;
  font-size: 11px;
  color: #64748b;
  text-transform: uppercase;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.recommendation-metrics strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
  color: #0f172a;
  letter-spacing: -0.03em;
}

.reason {
  margin-top: 14px;
  color: #475569;
  font-size: 14px;
}
</style>
