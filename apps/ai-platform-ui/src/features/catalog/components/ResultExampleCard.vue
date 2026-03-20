<template>
  <div class="example-card">
    <div class="example-header">
      <span class="eyebrow">결과 예시</span>
      <h3>{{ headline }}</h3>
    </div>
    <p class="description">{{ description }}</p>
    <div class="items">
      <div v-for="item in items" :key="item" class="item">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <circle cx="7" cy="7" r="6" fill="#4f46e5"/>
          <path d="M4.5 7l2 2 3-3" stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>{{ item }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ headline: string; description: string; items: string[] }>()
</script>

<style scoped>
.example-card {
  padding: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-header {}

.eyebrow {
  display: block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #4f46e5;
  margin-bottom: 6px;
}

h3 { font-size: 16px; font-weight: 700; color: #0f172a; }
.description { font-size: 13px; color: #64748b; line-height: 1.6; }

.items { display: flex; flex-direction: column; gap: 8px; }

.item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}
</style>
