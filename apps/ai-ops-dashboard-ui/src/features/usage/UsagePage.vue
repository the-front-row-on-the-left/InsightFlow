<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">Usage</h1>
        <p class="page-subtitle">사용자와 팀 기준 사용량을 운영 관점에서 조회합니다.</p>
      </div>
      <div class="toolbar">
        <div class="field">
          <label>User ID</label>
          <input v-model="userId" type="text" />
        </div>
        <div class="field">
          <label>Team ID</label>
          <input v-model="teamId" type="text" />
        </div>
      </div>
    </div>

    <div class="split-grid">
      <section class="panel">
        <p class="panel-title">User Scope</p>
        <div v-if="userUsage" class="metric-row">
          <div class="metric-box">
            <span>Requests</span>
            <strong>{{ userUsage.summary.total_requests }}</strong>
          </div>
          <div class="metric-box">
            <span>Total Tokens</span>
            <strong>{{ userUsage.summary.total_tokens.toLocaleString() }}</strong>
          </div>
          <div class="metric-box">
            <span>Avg Latency</span>
            <strong>{{ userUsage.summary.avg_latency_ms }}ms</strong>
          </div>
        </div>
        <table v-if="userUsage" class="data-table">
          <thead>
            <tr>
              <th>Request</th>
              <th>Service</th>
              <th>Model</th>
              <th>Tokens</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in userUsage.items" :key="item.request_id">
              <td>{{ item.request_id }}</td>
              <td>{{ item.service_id }}</td>
              <td>{{ item.model }}</td>
              <td>{{ item.total_tokens }}</td>
              <td><span :class="statusClass(item.status)">{{ item.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <p class="panel-title">Team Scope</p>
        <div v-if="teamUsage" class="metric-row">
          <div class="metric-box">
            <span>Requests</span>
            <strong>{{ teamUsage.summary.total_requests }}</strong>
          </div>
          <div class="metric-box">
            <span>Succeeded</span>
            <strong>{{ teamUsage.summary.succeeded_requests }}</strong>
          </div>
          <div class="metric-box">
            <span>Blocked</span>
            <strong>{{ teamUsage.summary.blocked_requests }}</strong>
          </div>
        </div>
        <table v-if="teamUsage" class="data-table">
          <thead>
            <tr>
              <th>Service</th>
              <th>Workflow</th>
              <th>Policy</th>
              <th>Limit</th>
              <th>At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in teamUsage.items" :key="`team-${item.request_id}`">
              <td>{{ item.service_id }}</td>
              <td>{{ item.workflow_id }}</td>
              <td>{{ item.policy_result }}</td>
              <td>{{ item.limit_result }}</td>
              <td>{{ formatDate(item.requested_at) }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { getTeamUsage, getUserUsage } from '../platform/dashboardApi'
import type { UsageScopeResponse } from '../platform/types'

const userId = ref('u_demo_001')
const teamId = ref('t_demo')
const userUsage = ref<UsageScopeResponse | null>(null)
const teamUsage = ref<UsageScopeResponse | null>(null)

function statusClass(status: string) {
  if (status === 'SUCCEEDED') return 'pill pill-success'
  if (status === 'FAILED' || status === 'BLOCKED') return 'pill pill-danger'
  return 'pill pill-muted'
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('ko-KR')
}

async function loadUsage() {
  ;[userUsage.value, teamUsage.value] = await Promise.all([
    getUserUsage(userId.value),
    getTeamUsage(teamId.value),
  ])
}

onMounted(loadUsage)
watch([userId, teamId], loadUsage)
</script>
