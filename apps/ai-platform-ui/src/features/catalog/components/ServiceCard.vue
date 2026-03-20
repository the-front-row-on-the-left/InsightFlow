<template>
  <article class="service-card" @click="$emit('detail')">
    <div class="card-top">
      <div class="badges">
        <span
          v-for="badge in service.badges"
          :key="badge"
          :class="['badge', badgeClass(badge)]"
        >{{ badge }}</span>
      </div>
      <button
        type="button"
        class="icon-btn"
        title="워크플로우에 추가"
        @click.stop="$emit('workflow')"
      >
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M7 2v10M2 7h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <div class="card-body">
      <p class="category">{{ service.categoryLabel }}</p>
      <h3>{{ service.name }}</h3>
      <p class="description">{{ service.description }}</p>
    </div>

    <div class="card-meta">
      <div class="meta-row">
        <span class="meta-label">역할</span>
        <span class="meta-value">{{ service.workflowRole }}</span>
      </div>
      <div class="meta-row">
        <span class="meta-label">과금</span>
        <span class="meta-value">{{ service.pricingLabel }}</span>
      </div>
    </div>

    <button type="button" class="btn-execute" @click.stop="$emit('execute')">
      실행하기
      <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
        <path d="M5 3l4 4-4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
  </article>
</template>

<script setup lang="ts">
import type { CatalogServiceCardViewModel } from '../../platform/types'

defineProps<{
  service: CatalogServiceCardViewModel
}>()

defineEmits<{
  detail: []
  execute: []
  workflow: []
}>()

function badgeClass(badge: string) {
  if (badge === 'Live Demo') return 'badge-live'
  if (badge === 'Recommended') return 'badge-rec'
  return 'badge-default'
}
</script>

<style scoped>
.service-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 20px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: box-shadow 0.18s, border-color 0.18s, transform 0.18s;
}

.service-card:hover {
  border-color: #c7d2fe;
  box-shadow: 0 4px 20px rgba(79, 70, 229, 0.1);
  transform: translateY(-2px);
}

/* Top */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.badge {
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.badge-live {
  background: #dcfce7;
  color: #15803d;
}

.badge-rec {
  background: #ede9fe;
  color: #6d28d9;
}

.badge-default {
  background: #f1f5f9;
  color: #475569;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #e2e8f0;
  border-radius: 7px;
  background: #f8fafc;
  color: #94a3b8;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.15s;
}

.icon-btn:hover {
  background: #eff6ff;
  border-color: #a5b4fc;
  color: #4f46e5;
}

/* Body */
.card-body {
  flex: 1;
}

.category {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #4f46e5;
  margin-bottom: 5px;
}

h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 7px;
  letter-spacing: -0.01em;
  line-height: 1.3;
}

.description {
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

/* Meta */
.card-meta {
  display: flex;
  flex-direction: column;
  gap: 5px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.meta-label {
  color: #94a3b8;
  font-weight: 500;
  min-width: 28px;
}

.meta-value {
  color: #475569;
  font-weight: 500;
}

/* Execute */
.btn-execute {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-execute:hover {
  background: #4338ca;
}
</style>
