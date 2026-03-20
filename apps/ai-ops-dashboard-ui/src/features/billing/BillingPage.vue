<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">Billing</h1>
        <p class="page-subtitle">사용자와 팀의 과금 상태를 서비스별로 점검합니다.</p>
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

    <div class="card-grid">
      <article class="kpi-card" v-if="userBilling">
        <p class="kpi-label">User Total Cost</p>
        <p class="kpi-value">{{ userBilling.summary.total_cost }}</p>
        <p class="kpi-hint">{{ userBilling.currency }} · {{ userBilling.price_table_version }}</p>
      </article>
      <article class="kpi-card" v-if="teamBilling">
        <p class="kpi-label">Team Total Cost</p>
        <p class="kpi-value">{{ teamBilling.summary.total_cost }}</p>
        <p class="kpi-hint">{{ teamBilling.summary.item_count }} billable items</p>
      </article>
    </div>

    <div class="split-grid">
      <section class="panel">
        <p class="panel-title">User Billing Items</p>
        <table v-if="userBilling" class="data-table">
          <thead>
            <tr>
              <th>Request</th>
              <th>Service</th>
              <th>Cost</th>
              <th>Pricing</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in userBilling.items" :key="item.request_id">
              <td>{{ item.request_id }}</td>
              <td>{{ item.service_id }}</td>
              <td>{{ item.total_cost }}</td>
              <td>{{ item.pricing_model }}</td>
              <td><span :class="statusClass(item.status)">{{ item.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <p class="panel-title">Team Billing Breakdown</p>
        <table v-if="teamBilling" class="data-table">
          <thead>
            <tr>
              <th>Service</th>
              <th>Model</th>
              <th>Tokens</th>
              <th>Cost</th>
              <th>At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in teamBilling.items" :key="`team-${item.request_id}`">
              <td>{{ item.service_id }}</td>
              <td>{{ item.model }}</td>
              <td>{{ item.total_tokens }}</td>
              <td>{{ item.total_cost }}</td>
              <td>{{ formatDate(item.occurred_at) }}</td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { getTeamBilling, getUserBilling } from '../platform/dashboardApi'
import type { BillingScopeResponse } from '../platform/types'

const userId = ref('u_demo_001')
const teamId = ref('t_demo')
const userBilling = ref<BillingScopeResponse | null>(null)
const teamBilling = ref<BillingScopeResponse | null>(null)

function statusClass(status: string) {
  if (status === 'SUCCEEDED') return 'pill pill-success'
  if (status === 'FAILED' || status === 'BLOCKED') return 'pill pill-danger'
  return 'pill pill-muted'
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('ko-KR')
}

async function loadBilling() {
  ;[userBilling.value, teamBilling.value] = await Promise.all([
    getUserBilling(userId.value),
    getTeamBilling(teamId.value),
  ])
}

onMounted(loadBilling)
watch([userId, teamId], loadBilling)
</script>
