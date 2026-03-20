<template>
  <div :class="['error-banner', `error-banner--${variant}`]">
    <div class="error-icon">
      <svg v-if="variant === 'policy'" width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M8 2L14 13H2L8 2Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
        <path d="M8 6.5v3M8 11.5v.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <svg v-else-if="variant === 'limit'" width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
        <path d="M8 5v4M8 11v.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <svg v-else width="16" height="16" viewBox="0 0 16 16" fill="none">
        <circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.5"/>
        <path d="M6 6l4 4M10 6l-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </div>
    <div class="error-content">
      <p class="error-title">{{ title }}</p>
      <p class="error-message">{{ message }}</p>
      <p v-if="requestId" class="error-meta">요청 ID: {{ requestId }}</p>
    </div>
  </div>
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
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid transparent;
}

.error-banner--policy { background: #fffbeb; border-color: #fde68a; color: #92400e; }
.error-banner--limit  { background: #fff7ed; border-color: #fed7aa; color: #9a3412; }
.error-banner--server { background: #f5f3ff; border-color: #ddd6fe; color: #4c1d95; }

.error-icon { flex-shrink: 0; padding-top: 1px; }
.error-content { display: flex; flex-direction: column; gap: 3px; }
.error-title { font-size: 14px; font-weight: 700; }
.error-message { font-size: 13px; opacity: 0.85; line-height: 1.5; }
.error-meta { font-size: 11px; opacity: 0.6; font-family: 'SF Mono', monospace; }
</style>
