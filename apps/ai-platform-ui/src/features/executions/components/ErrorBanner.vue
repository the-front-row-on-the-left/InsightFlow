<template>
  <aside :class="['error-banner', variant]">
    <p class="label">{{ title }}</p>
    <p class="message">{{ message }}</p>
    <p v-if="requestId" class="request-id">request_id: {{ requestId }}</p>
  </aside>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  message: string
  requestId?: string
  variant?: 'policy' | 'limit' | 'server'
}>()
</script>

<style scoped>
.error-banner {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid transparent;
}

.policy {
  background: #fff0d7;
  border-color: #db9c2e;
}

.limit {
  background: #ffe3d8;
  border-color: #d05c3f;
}

.server {
  background: #ece7ff;
  border-color: #6f57cb;
}

.label,
.message,
.request-id {
  margin: 0;
}

.label {
  font-weight: 700;
}

.message,
.request-id {
  margin-top: 6px;
}
</style>
