<template>
  <article class="service-card">
    <div class="card-header">
      <p class="category">{{ service.categoryLabel }}</p>
      <div class="badges">
        <span v-for="badge in service.badges" :key="badge">{{ badge }}</span>
      </div>
    </div>

    <h3>{{ service.name }}</h3>
    <p class="description">{{ service.description }}</p>

    <dl class="facts">
      <div>
        <dt>Pricing</dt>
        <dd>{{ service.pricingLabel }}</dd>
      </div>
      <div>
        <dt>Models</dt>
        <dd>{{ service.modelSummary }}</dd>
      </div>
      <div>
        <dt>Workflow Role</dt>
        <dd>{{ service.workflowRole }}</dd>
      </div>
    </dl>

    <div class="actions">
      <button type="button" class="ghost" @click="$emit('detail')">상세 보기</button>
      <button type="button" @click="$emit('execute')">바로 실행</button>
      <button type="button" class="secondary" @click="$emit('workflow')">워크플로우에 추가</button>
    </div>
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
</script>

<style scoped>
.service-card {
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
  box-shadow: 0 18px 36px rgba(68, 53, 24, 0.08);
}

.card-header,
.actions {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.category {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

.badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.badges span {
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(31, 31, 31, 0.08);
  font-size: 12px;
}

h3 {
  margin: 14px 0 8px;
  font-size: 26px;
}

.description {
  min-height: 52px;
  margin: 0;
  line-height: 1.6;
  color: #514a40;
}

.facts {
  margin: 18px 0;
  display: grid;
  gap: 10px;
}

.facts div {
  display: grid;
  gap: 3px;
}

dt {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #8b8177;
}

dd {
  margin: 0;
  font-weight: 600;
}

.actions {
  flex-wrap: wrap;
}

button {
  font: inherit;
  border: 0;
  border-radius: 999px;
  padding: 10px 14px;
  cursor: pointer;
}

button {
  background: #1f1f1f;
  color: #f7f1e6;
}

.ghost,
.secondary {
  color: #1f1f1f;
}

.ghost {
  background: rgba(31, 31, 31, 0.08);
}

.secondary {
  background: rgba(128, 89, 42, 0.18);
}
</style>
