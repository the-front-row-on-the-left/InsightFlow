<template>
  <section v-if="service" class="detail-page">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
      <RouterLink to="/catalog">카탈로그</RouterLink>
      <span class="sep">›</span>
      <span>{{ service.name }}</span>
    </nav>

    <!-- Hero -->
    <div class="hero">
      <div class="hero-left">
        <div class="hero-badges">
          <span
            v-for="tag in service.tags"
            :key="tag"
            class="tag"
          >#{{ tag }}</span>
        </div>
        <h1>{{ service.name }}</h1>
        <p class="description">{{ service.description }}</p>
        <div class="hero-actions">
          <button type="button" class="btn-primary" @click="goToExecute">
            실행하기
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M5 3l4 4-4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <button type="button" class="btn-secondary" @click="addToWorkflow">+ 워크플로우에 추가</button>
        </div>
      </div>
      <div class="hero-right">
        <div class="info-card">
          <div class="info-row">
            <span class="info-label">서비스 ID</span>
            <code class="info-code">{{ service.service_id }}</code>
          </div>
          <div class="divider"></div>
          <div class="info-row">
            <span class="info-label">과금 방식</span>
            <span class="info-value">{{ service.pricing_model }}</span>
          </div>
          <div class="divider"></div>
          <div class="info-row">
            <span class="info-label">워크플로우 역할</span>
            <span class="info-value">{{ service.workflow_role }}</span>
          </div>
          <div class="divider"></div>
          <div class="info-row-col">
            <span class="info-label">지원 모델</span>
            <div class="model-chips">
              <span v-for="m in service.supported_models" :key="m" class="model-chip">{{ m }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom grid -->
    <div class="bottom-grid">
      <div class="panel">
        <h2 class="panel-title">입력 예시</h2>
        <div class="examples-list">
          <div v-for="ex in service.input_examples" :key="ex.label" class="example-item">
            <span class="example-label">{{ ex.label }}</span>
            <span class="example-value">{{ ex.value }}</span>
          </div>
        </div>
      </div>

      <ResultExampleCard
        :headline="service.result_example.headline"
        :description="service.result_example.description"
        :items="service.result_example.items"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { getCatalogServiceDetail } from '../platform/catalogApi'
import type { ServiceDetailDto, ServiceId } from '../platform/types'
import { useWorkflowDraft } from '../workflows/useWorkflowDraft'
import ResultExampleCard from './components/ResultExampleCard.vue'

const route = useRoute()
const router = useRouter()
const { addStep } = useWorkflowDraft()
const service = ref<ServiceDetailDto | null>(null)

async function loadService() {
  service.value = await getCatalogServiceDetail(route.params.serviceId as ServiceId)
}

function goToExecute() {
  if (service.value) router.push(`/execute/${service.value.service_id}`)
}

async function addToWorkflow() {
  if (service.value) {
    addStep(service.value.service_id)
    await router.push('/workflows/new')
  }
}

onMounted(loadService)
watch(() => route.params.serviceId, loadService)
</script>

<style scoped>
.detail-page { display: flex; flex-direction: column; gap: 20px; }

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #94a3b8;
}

.breadcrumb a { color: #64748b; transition: color 0.15s; }
.breadcrumb a:hover { color: #4f46e5; }
.sep { color: #cbd5e1; }

/* Hero */
.hero {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  padding: 28px;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.hero-badges { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 12px; }

.tag {
  padding: 3px 9px;
  border-radius: 999px;
  background: #eff6ff;
  color: #4f46e5;
  font-size: 11px;
  font-weight: 600;
}

h1 {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.2;
  margin-bottom: 10px;
}

.description { font-size: 15px; color: #475569; line-height: 1.65; margin-bottom: 20px; }

.hero-actions { display: flex; gap: 10px; flex-wrap: wrap; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-primary:hover { background: #4338ca; }

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}
.btn-secondary:hover { background: #f8fafc; border-color: #c7d2fe; }

/* Info card */
.info-card {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.info-row-col {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
}

.divider { height: 1px; background: #e2e8f0; }

.info-label { font-size: 12px; color: #94a3b8; font-weight: 500; }
.info-value { font-size: 13px; color: #0f172a; font-weight: 600; text-align: right; }
.info-code {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 12px;
  background: #ede9fe;
  color: #5b21b6;
  padding: 2px 7px;
  border-radius: 5px;
}

.model-chips { display: flex; gap: 6px; flex-wrap: wrap; }
.model-chip {
  padding: 3px 9px;
  border-radius: 999px;
  background: #e0e7ff;
  color: #4338ca;
  font-size: 11px;
  font-weight: 600;
}

/* Bottom grid */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.panel {
  padding: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.panel-title {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 14px;
}

.examples-list { display: flex; flex-direction: column; gap: 10px; }

.example-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px;
  background: #f8fafc;
  border-radius: 8px;
}

.example-label { font-size: 11px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; }
.example-value { font-size: 13px; color: #334155; }

@media (max-width: 900px) {
  .hero { grid-template-columns: 1fr; }
}

@media (max-width: 720px) {
  .bottom-grid { grid-template-columns: 1fr; }
}
</style>
