<template>
  <section v-if="service" class="detail-page">
    <div class="hero">
      <div>
        <p class="eyebrow">{{ service.category }}</p>
        <h2>{{ service.name }}</h2>
        <p class="description">{{ service.description }}</p>
      </div>
      <div class="actions">
        <button type="button" class="secondary" @click="addToWorkflow">워크플로우에 추가</button>
        <button type="button" @click="goToExecute">바로 실행</button>
      </div>
    </div>

    <div class="detail-grid">
      <section class="panel">
        <h3>서비스 정보</h3>
        <dl>
          <div>
            <dt>service_id</dt>
            <dd>{{ service.service_id }}</dd>
          </div>
          <div>
            <dt>pricing_model</dt>
            <dd>{{ service.pricing_model }}</dd>
          </div>
          <div>
            <dt>supported_models</dt>
            <dd>{{ service.supported_models.join(', ') }}</dd>
          </div>
          <div>
            <dt>workflow_role</dt>
            <dd>{{ service.workflow_role }}</dd>
          </div>
        </dl>
      </section>

      <section class="panel">
        <h3>입력 예시</h3>
        <ul class="examples">
          <li v-for="example in service.input_examples" :key="example.label">
            <strong>{{ example.label }}</strong>
            <span>{{ example.value }}</span>
          </li>
        </ul>
      </section>
    </div>

    <ResultExampleCard
      :headline="service.result_example.headline"
      :description="service.result_example.description"
      :items="service.result_example.items"
    />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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
  if (!service.value) {
    return
  }

  router.push(`/execute/${service.value.service_id}`)
}

async function addToWorkflow() {
  if (!service.value) {
    return
  }

  addStep(service.value.service_id)
  await router.push('/workflows/new')
}

onMounted(loadService)
watch(() => route.params.serviceId, loadService)
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 18px;
}

.hero,
.detail-grid {
  display: grid;
  gap: 16px;
}

.hero,
.panel {
  padding: 22px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
  box-shadow: 0 18px 34px rgba(68, 53, 24, 0.08);
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h2 {
  margin: 8px 0 12px;
  font-size: clamp(30px, 4vw, 48px);
}

.description {
  margin: 0;
  max-width: 760px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.actions button {
  font: inherit;
  border: 0;
  border-radius: 999px;
  padding: 11px 16px;
  cursor: pointer;
  background: #1f1f1f;
  color: #f7f1e6;
}

.actions .secondary {
  background: rgba(31, 31, 31, 0.08);
  color: #1f1f1f;
}

.detail-grid {
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

h3 {
  margin: 0 0 14px;
}

dl,
.examples {
  margin: 0;
  display: grid;
  gap: 12px;
}

dt {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8b8177;
}

dd {
  margin: 4px 0 0;
  font-weight: 600;
}

.examples {
  padding-left: 18px;
}

.examples li {
  display: grid;
  gap: 4px;
}

.examples span {
  color: #514a40;
}
</style>
