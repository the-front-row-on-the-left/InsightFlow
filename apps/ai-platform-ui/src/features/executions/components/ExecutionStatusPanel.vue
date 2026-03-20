<template>
  <div :class="['status-panel', `status-panel--${statusVariant}`]">
    <div class="status-left">
      <span class="status-dot"></span>
      <div>
        <p class="status-label">실행 상태</p>
        <p class="status-text">{{ statusLabel }}</p>
      </div>
    </div>
    <div class="status-right">
      <div class="stat">
        <span class="stat-label">비용</span>
        <span class="stat-value">{{ costLabel }}</span>
      </div>
      <div class="stat">
        <span class="stat-label">요청 ID</span>
        <code class="stat-code">{{ requestId }}</code>
      </div>
      <div v-if="recommendationMessage" class="stat">
        <span class="stat-label">권고</span>
        <span class="stat-value">{{ recommendationMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
  requestId: string
  costStatus: string
  recommendationMessage: string
}>()

const statusVariant = computed(() => {
  if (props.status === 'SUCCEEDED') return 'success'
  if (props.status === 'FAILED') return 'error'
  if (props.status === 'RUNNING') return 'running'
  return 'pending'
})

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    SUCCEEDED: '실행 완료',
    FAILED: '실행 실패',
    RUNNING: '처리 중...',
    PENDING: '대기 중',
  }
  return map[props.status] ?? props.status
})

const costLabel = computed(() => {
  if (props.costStatus === 'completed' || props.costStatus === 'READY') return '계산 완료'
  if (props.costStatus === 'estimated') return '추정값'
  return '계산 중'
})
</script>

<style scoped>
.status-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid transparent;
  flex-wrap: wrap;
}

.status-panel--success { background: #f0fdf4; border-color: #bbf7d0; }
.status-panel--error   { background: #fef2f2; border-color: #fecaca; }
.status-panel--running { background: #eff6ff; border-color: #bfdbfe; }
.status-panel--pending { background: #f8fafc; border-color: #e2e8f0; }

.status-left { display: flex; align-items: center; gap: 12px; }

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-panel--success .status-dot { background: #22c55e; }
.status-panel--error   .status-dot { background: #ef4444; }
.status-panel--running .status-dot { background: #3b82f6; animation: pulse 1.2s ease infinite; }
.status-panel--pending .status-dot { background: #94a3b8; }

@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:0.4; } }

.status-label { font-size: 11px; color: #94a3b8; font-weight: 500; margin-bottom: 2px; }
.status-text { font-size: 15px; font-weight: 700; color: #0f172a; }

.status-right { display: flex; gap: 20px; flex-wrap: wrap; }

.stat { display: flex; flex-direction: column; gap: 2px; }
.stat-label { font-size: 11px; color: #94a3b8; font-weight: 500; }
.stat-value { font-size: 13px; color: #334155; font-weight: 600; }
.stat-code { font-family: 'SF Mono', 'Fira Code', monospace; font-size: 11px; color: #6d28d9; background: #ede9fe; padding: 2px 6px; border-radius: 4px; }
</style>
