<template>
  <section class="catalog-page">
    <div class="toolbar">
      <div>
        <p class="eyebrow">Service Catalog</p>
        <h2>실행 가능한 AI 서비스를 탐색합니다.</h2>
      </div>
      <label class="search-field">
        <span>검색</span>
        <input v-model="keyword" type="search" placeholder="문서, 보고서, assistant" />
      </label>
    </div>

    <p class="summary">총 {{ cards.length }}개 서비스</p>

    <div class="card-grid">
      <ServiceCard
        v-for="service in cards"
        :key="service.id"
        :service="service"
        @detail="goToDetail(service.id)"
        @execute="goToExecute(service.id)"
        @workflow="addToWorkflow(service.id)"
      />
    </div>

    <p v-if="workflowMessage" class="workflow-message">{{ workflowMessage }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { getCatalogServices } from '../platform/catalogApi'
import type { CatalogServiceCardViewModel, ServiceCatalogItemDto, ServiceId } from '../platform/types'
import { toCatalogCards } from '../platform/viewModels'
import { useWorkflowDraft } from '../workflows/useWorkflowDraft'
import ServiceCard from './components/ServiceCard.vue'

const router = useRouter()
const { addStep } = useWorkflowDraft()

const keyword = ref('')
const services = ref<ServiceCatalogItemDto[]>([])
const workflowMessage = ref('')

const cards = computed<CatalogServiceCardViewModel[]>(() => toCatalogCards(services.value))

async function loadServices() {
  services.value = await getCatalogServices({ keyword: keyword.value })
}

function goToDetail(serviceId: ServiceId) {
  router.push(`/catalog/${serviceId}`)
}

function goToExecute(serviceId: ServiceId) {
  router.push(`/execute/${serviceId}`)
}

async function addToWorkflow(serviceId: ServiceId) {
  addStep(serviceId)
  workflowMessage.value = `${serviceId} step을 draft에 추가했습니다.`
  await router.push('/workflows/new')
}

onMounted(loadServices)
watch(keyword, loadServices)
</script>

<style scoped>
.catalog-page {
  display: grid;
  gap: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: end;
}

.eyebrow,
.summary,
.workflow-message {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h2 {
  margin: 8px 0 0;
  font-size: clamp(28px, 3.4vw, 42px);
}

.search-field {
  display: grid;
  gap: 8px;
  min-width: 280px;
}

.search-field span {
  font-weight: 600;
}

input {
  font: inherit;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(31, 31, 31, 0.1);
  background: rgba(255, 255, 255, 0.8);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

@media (max-width: 720px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-field {
    min-width: 0;
  }
}
</style>
