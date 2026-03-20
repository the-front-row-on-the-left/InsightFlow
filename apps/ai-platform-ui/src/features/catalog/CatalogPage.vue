<template>
  <section class="catalog-page">
    <div class="page-header">
      <div>
        <h1>AI 서비스 카탈로그</h1>
        <p class="subtitle">실행 가능한 AI 서비스를 탐색하고 바로 실행하거나 워크플로우로 조합하세요.</p>
      </div>
      <div class="search-wrapper">
        <svg class="search-icon" width="15" height="15" viewBox="0 0 15 15" fill="none">
          <circle cx="6.5" cy="6.5" r="5" stroke="#94a3b8" stroke-width="1.5"/>
          <path d="M10.5 10.5l3 3" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <input v-model="keyword" type="search" placeholder="서비스 검색..." class="search-input" />
      </div>
    </div>

    <div class="stats-bar">
      <span class="stats-count">총 {{ cards.length }}개 서비스</span>
    </div>

    <div v-if="loadError" class="error-banner">
      <strong>카탈로그를 불러오지 못했습니다.</strong>
      <span>{{ loadError }}</span>
    </div>

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

    <Transition name="toast">
      <div v-if="workflowMessage" class="toast">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="7" fill="#4f46e5"/>
          <path d="M5 8l2.5 2.5L11 5.5" stroke="#fff" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        {{ workflowMessage }}
      </div>
    </Transition>
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
const loadError = ref('')

const cards = computed<CatalogServiceCardViewModel[]>(() => toCatalogCards(services.value))

async function loadServices() {
  loadError.value = ''

  try {
    services.value = await getCatalogServices({ keyword: keyword.value })
  } catch (error) {
    services.value = []
    loadError.value = error instanceof Error ? error.message : '서비스 목록 요청에 실패했습니다.'
  }
}

function goToDetail(serviceId: ServiceId) { router.push(`/catalog/${serviceId}`) }
function goToExecute(serviceId: ServiceId) { router.push(`/execute/${serviceId}`) }

async function addToWorkflow(serviceId: ServiceId) {
  addStep(serviceId)
  workflowMessage.value = '워크플로우 draft에 추가했습니다.'
  setTimeout(() => { workflowMessage.value = '' }, 2500)
  await router.push('/workflows/new')
}

onMounted(loadServices)
watch(keyword, loadServices)
</script>

<style scoped>
.catalog-page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  flex-wrap: wrap;
}

h1 {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.03em;
  line-height: 1.2;
  margin-bottom: 6px;
}

.subtitle { font-size: 14px; color: #64748b; }

.search-wrapper { position: relative; display: flex; align-items: center; }
.search-icon { position: absolute; left: 12px; pointer-events: none; }

.search-input {
  padding: 9px 14px 9px 36px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 14px;
  color: #0f172a;
  width: 220px;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.search-input::placeholder { color: #94a3b8; }
.search-input:focus { border-color: #a5b4fc; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }

.stats-bar { display: flex; align-items: center; }
.stats-count { font-size: 13px; color: #94a3b8; font-weight: 500; }

.error-banner {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid #fecaca;
  border-radius: 12px;
  background: #fef2f2;
  color: #991b1b;
  font-size: 13px;
}

.error-banner strong {
  font-size: 13px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.toast {
  position: fixed;
  bottom: 32px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  background: #0f172a;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 8px 24px rgba(0,0,0,0.18);
  z-index: 200;
  white-space: nowrap;
}

.toast-enter-active, .toast-leave-active { transition: opacity 0.25s, transform 0.25s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translateX(-50%) translateY(10px); }

@media (max-width: 720px) {
  .page-header { flex-direction: column; }
  .search-input { width: 100%; }
  .search-wrapper { width: 100%; }
}
</style>
