<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">Executions</h1>
        <p class="page-subtitle">서비스 실행 요청의 출처, 상태, 처리 시간을 주기적으로 갱신합니다.</p>
      </div>
      <div class="page-meta">
        <span class="pill pill-info">Polling every 5s</span>
      </div>
    </div>

    <section class="panel">
      <p class="panel-title">Execution Stream</p>
      <table v-if="executions.length > 0" class="data-table">
        <thead>
          <tr>
            <th>Execution</th>
            <th>Source</th>
            <th>Service</th>
            <th>Status</th>
            <th>Started</th>
            <th>Finished</th>
            <th>Duration</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in executions" :key="item.execution_id">
            <td>
              <strong>{{ item.execution_id }}</strong>
              <div class="cell-sub">{{ item.request_id }}</div>
            </td>
            <td>{{ item.source }}</td>
            <td>
              <div>{{ item.service_id }}</div>
              <div class="cell-sub">{{ item.model }}</div>
              <div v-if="item.error_message" class="cell-error">{{ item.error_message }}</div>
            </td>
            <td><span :class="statusClass(item.status)">{{ item.status }}</span></td>
            <td>{{ formatDate(item.started_at) }}</td>
            <td>{{ item.finished_at ? formatDate(item.finished_at) : 'running' }}</td>
            <td>{{ item.duration_ms ? `${item.duration_ms}ms` : '...' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">실행 이력이 아직 없습니다.</div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

import { getRecentExecutions } from '../platform/dashboardApi'
import type { ExecutionItem } from '../platform/types'

const executions = ref<ExecutionItem[]>([])
let timer: number | null = null

function formatDate(value: string) {
  return new Date(value).toLocaleString('ko-KR')
}

function statusClass(status: string) {
  if (status === 'SUCCEEDED') return 'pill pill-success'
  if (status === 'FAILED' || status === 'BLOCKED') return 'pill pill-danger'
  if (status === 'RUNNING') return 'pill pill-info'
  return 'pill pill-muted'
}

async function loadExecutions() {
  executions.value = await getRecentExecutions()
}

onMounted(async () => {
  await loadExecutions()
  timer = window.setInterval(loadExecutions, 5000)
})

onBeforeUnmount(() => {
  if (timer !== null) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.page-meta {
  display: flex;
  align-items: center;
}

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
</style>
