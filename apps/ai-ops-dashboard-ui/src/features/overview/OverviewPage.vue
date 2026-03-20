<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">AIOps Overview</h1>
        <p class="page-subtitle">운영 지표, 최근 실행, 알림 상태를 한 화면에서 빠르게 확인합니다.</p>
      </div>
    </div>

    <div v-if="overview" class="card-grid">
      <article class="kpi-card">
        <p class="kpi-label">Total Requests</p>
        <p class="kpi-value">{{ overview.stats.total_requests }}</p>
        <p class="kpi-hint">최근 범위의 전체 실행 요청</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Total Tokens</p>
        <p class="kpi-value">{{ overview.stats.total_tokens.toLocaleString() }}</p>
        <p class="kpi-hint">사용자 기준 총 토큰 사용량</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Total Cost</p>
        <p class="kpi-value">{{ overview.stats.total_cost }}</p>
        <p class="kpi-hint">누적 과금 비용</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Avg Latency</p>
        <p class="kpi-value">{{ overview.stats.avg_latency_ms }}ms</p>
        <p class="kpi-hint">사용 요청 평균 처리 시간</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Active Alerts</p>
        <p class="kpi-value">{{ overview.stats.active_alerts }}</p>
        <p class="kpi-hint">미해결 알림 개수</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Blocked Requests</p>
        <p class="kpi-value">{{ overview.usage.summary.blocked_requests }}</p>
        <p class="kpi-hint">정책 또는 quota로 차단된 요청</p>
      </article>
      <article class="kpi-card">
        <p class="kpi-label">Recommendations</p>
        <p class="kpi-value">{{ overview.stats.recommendation_count }}</p>
        <p class="kpi-hint">현재 노출 가능한 절감 제안</p>
      </article>
    </div>

    <div class="split-grid">
      <section class="panel">
        <p class="panel-title">Recent Executions</p>
        <div v-if="overview && overview.recent_executions.length > 0">
          <table class="data-table">
            <thead>
              <tr>
                <th>Service</th>
                <th>Status</th>
                <th>Source</th>
                <th>Duration</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in overview.recent_executions" :key="item.execution_id">
                <td>
                  <strong>{{ item.service_id }}</strong>
                  <div class="cell-sub">{{ item.request_id }}</div>
                  <div v-if="item.error_message" class="cell-error">{{ item.error_message }}</div>
                </td>
                <td><span :class="statusClass(item.status)">{{ item.status }}</span></td>
                <td>{{ item.source }}</td>
                <td>{{ item.duration_ms ? `${item.duration_ms}ms` : 'running' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="empty-state">표시할 실행 이력이 없습니다.</div>
      </section>

      <section class="panel">
        <p class="panel-title">Signals</p>
        <div v-if="overview" class="signal-stack">
          <div class="signal-card">
            <span>Top Cost Pressure</span>
            <strong>{{ topCostService }}</strong>
            <p>현재 과금 항목 중 비용이 가장 큰 서비스</p>
          </div>
          <div class="signal-card">
            <span>Open Alert</span>
            <strong>{{ latestAlertTitle }}</strong>
            <p>가장 최근의 운영 알림 제목</p>
          </div>
          <div class="signal-card">
            <span>Optimization Hint</span>
            <strong>{{ latestRecommendation }}</strong>
            <p>추천 서비스에서 받은 최신 절감 제안</p>
          </div>
          <div class="signal-card">
            <span>Rate Limit Status</span>
            <strong>{{ limitSignal }}</strong>
            <p>현재 누적 차단 요청 수와 제한 압력 신호</p>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import { getOverviewData } from '../platform/dashboardApi'
import type { OverviewData } from '../platform/types'

const overview = ref<OverviewData | null>(null)
let timer: number | null = null

const topCostService = computed(() => overview.value?.billing.items[0]?.service_id ?? 'N/A')
const latestAlertTitle = computed(() => overview.value?.notifications[0]?.title ?? 'No active alerts')
const latestRecommendation = computed(
  () => overview.value?.recommendations.recommendations[0]?.recommended_model ?? 'No recommendation',
)
const limitSignal = computed(() => {
  const blocked = overview.value?.usage.summary.blocked_requests ?? 0
  if (blocked === 0) return 'Healthy'
  return `${blocked} blocked`
})

function statusClass(status: string) {
  if (status === 'SUCCEEDED') return 'pill pill-success'
  if (status === 'FAILED' || status === 'BLOCKED') return 'pill pill-danger'
  if (status === 'RUNNING') return 'pill pill-info'
  return 'pill pill-muted'
}

async function loadOverview() {
  overview.value = await getOverviewData()
}

onMounted(async () => {
  await loadOverview()
  timer = window.setInterval(loadOverview, 5000)
})

onBeforeUnmount(() => {
  if (timer !== null) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.cell-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
  font-family: 'SF Mono', monospace;
}

.cell-error {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #b91c1c;
}

.signal-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.signal-card {
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.signal-card span {
  display: block;
  font-size: 12px;
  color: #64748b;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.signal-card strong {
  display: block;
  margin-top: 8px;
  font-size: 20px;
  color: #0f172a;
  letter-spacing: -0.03em;
}

.signal-card p {
  margin-top: 6px;
  font-size: 13px;
  color: #64748b;
}
</style>
