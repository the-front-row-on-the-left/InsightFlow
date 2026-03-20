<template>
  <section class="page-stack">
    <div class="page-header">
      <div>
        <h1 class="page-title">Notifications</h1>
        <p class="page-subtitle">사용량 제한 초과와 비용 이상 징후를 운영자가 추적합니다.</p>
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
        <p class="panel-title">Subscriptions</p>
        <table v-if="subscriptions.length > 0" class="data-table">
          <thead>
            <tr>
              <th>Event</th>
              <th>Channel</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in subscriptions" :key="`${item.event_type}-${item.channel}`">
              <td>{{ item.event_type }}</td>
              <td>{{ item.channel }}</td>
              <td><span class="pill pill-info">{{ item.status }}</span></td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="panel">
        <p class="panel-title">Recent Alerts</p>
        <div v-if="notifications.length > 0" class="notification-list">
          <article v-for="item in notifications" :key="item.notification_id" class="notification-card">
            <div class="notification-top">
              <strong>{{ item.title }}</strong>
              <span :class="item.event_type === 'limit.exceeded' ? 'pill pill-danger' : 'pill pill-warning'">
                {{ item.event_type }}
              </span>
            </div>
            <p>{{ item.message }}</p>
            <div class="meta-row">
              <span>{{ item.recipient_type }}: {{ item.recipient_id }}</span>
              <span>{{ formatDate(item.occurred_at) }}</span>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">표시할 알림이 없습니다.</div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import { getNotifications, getNotificationSubscriptions } from '../platform/dashboardApi'
import type { InternalNotification, NotificationSubscription } from '../platform/types'

const userId = ref('u_demo_001')
const teamId = ref('t_demo')
const notifications = ref<InternalNotification[]>([])
const subscriptions = ref<NotificationSubscription[]>([])

function formatDate(value: string) {
  return new Date(value).toLocaleString('ko-KR')
}

async function loadNotifications() {
  ;[notifications.value, subscriptions.value] = await Promise.all([
    getNotifications(userId.value, teamId.value),
    getNotificationSubscriptions(userId.value, teamId.value),
  ])
}

onMounted(loadNotifications)
watch([userId, teamId], loadNotifications)
</script>

<style scoped>
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-card {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.notification-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.notification-card p {
  margin-top: 10px;
  font-size: 14px;
  color: #475569;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  font-size: 12px;
  color: #94a3b8;
  flex-wrap: wrap;
}
</style>
