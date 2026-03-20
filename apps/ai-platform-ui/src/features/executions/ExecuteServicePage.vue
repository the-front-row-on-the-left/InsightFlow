<template>
  <section v-if="service" class="execute-page">
    <nav class="breadcrumb">
      <RouterLink to="/catalog">카탈로그</RouterLink>
      <span class="sep">›</span>
      <RouterLink :to="`/catalog/${service.service_id}`">{{ service.name }}</RouterLink>
      <span class="sep">›</span>
      <span>실행</span>
    </nav>

    <div class="layout">
      <div class="form-col">
        <div class="form-header">
          <h1>{{ service.name }} 실행</h1>
          <p class="form-desc">{{ service.short_description }}</p>
        </div>

        <ErrorBanner
          v-if="bannerError"
          :title="bannerError.title"
          :message="bannerError.message"
          :request-id="bannerError.requestId"
          :variant="bannerError.variant"
        />

        <div class="form-panel">
          <ExecutionForm
            :service-id="service.service_id"
            :loading="submitting"
            :inline-errors="inlineErrors"
            @submit="handleSubmit"
          />
        </div>
      </div>

      <aside class="side-col">
        <div class="side-card">
          <p class="side-title">서비스 정보</p>
          <div class="side-row">
            <span class="side-label">카테고리</span>
            <span class="side-value">{{ service.category }}</span>
          </div>
          <div class="side-row">
            <span class="side-label">실행 방식</span>
            <span :class="['mode-badge', service.execution_mode === 'real' ? 'mode-live' : 'mode-mock']">
              {{ service.execution_mode === 'real' ? 'Live Demo' : 'Mock' }}
            </span>
          </div>
          <div class="side-row">
            <span class="side-label">과금</span>
            <span class="side-value">{{ service.pricing_model }}</span>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { ApiRequestError } from '../../lib/api'
import { getCatalogServiceDetail } from '../platform/catalogApi'
import { createExecution, getExecutionDetail } from '../platform/executionApi'
import type { ServiceDetailDto, ServiceId } from '../platform/types'
import ErrorBanner from './components/ErrorBanner.vue'
import ExecutionForm from './components/ExecutionForm.vue'

const route = useRoute()
const router = useRouter()
const service = ref<ServiceDetailDto | null>(null)
const submitting = ref(false)
const inlineErrors = ref<Record<string, string>>({})
const bannerError = ref<null | { title: string; message: string; requestId: string; variant: 'policy' | 'limit' | 'server' }>(null)

const serviceId = computed(() => route.params.serviceId as ServiceId)

async function loadService() {
  service.value = await getCatalogServiceDetail(serviceId.value)
  inlineErrors.value = {}
  bannerError.value = null
}

async function handleSubmit(input: Record<string, unknown>) {
  inlineErrors.value = {}
  bannerError.value = null
  submitting.value = true
  try {
    const execution = await createExecution({ service_id: serviceId.value, model: service.value?.supported_models[0] ?? 'gpt-5.4-mini', input })
    if (execution.status === 'FAILED') {
      const failed = await getExecutionDetail(execution.execution_id)
      if (failed.error?.code === 'INVALID_REQUEST') {
        inlineErrors.value = failed.error.details ?? { general: failed.error.message }
      } else {
        bannerError.value = { title: '실행 실패', message: failed.error?.message ?? '알 수 없는 오류', requestId: failed.request_id, variant: 'server' }
      }
      return
    }
    await router.push(`/executions/${execution.execution_id}`)
  } catch (error) {
    if (error instanceof ApiRequestError) {
      if (error.code === 'INVALID_REQUEST') { inlineErrors.value = error.details ?? {} }
      else if (error.code === 'POLICY_BLOCKED') { bannerError.value = { title: '정책 차단', message: error.message, requestId: error.requestId, variant: 'policy' } }
      else if (error.code === 'RATE_LIMIT_EXCEEDED') { bannerError.value = { title: '요청 제한 초과', message: error.message, requestId: error.requestId, variant: 'limit' } }
      else { bannerError.value = { title: '실행 실패', message: error.message, requestId: error.requestId, variant: 'server' } }
    } else {
      bannerError.value = { title: '실행 실패', message: '알 수 없는 오류가 발생했습니다.', requestId: 'req_unknown', variant: 'server' }
    }
  } finally { submitting.value = false }
}

onMounted(loadService)
watch(serviceId, loadService)
</script>

<style scoped>
.execute-page { display: flex; flex-direction: column; gap: 20px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #94a3b8; }
.breadcrumb a { color: #64748b; transition: color 0.15s; }
.breadcrumb a:hover { color: #4f46e5; }
.sep { color: #cbd5e1; }

.layout {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
  align-items: start;
}

.form-header { margin-bottom: 16px; }

h1 {
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.form-desc { font-size: 14px; color: #64748b; }

.form-panel {
  padding: 24px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

/* Side */
.side-card {
  padding: 18px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.side-title {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #94a3b8;
  margin-bottom: 12px;
}

.side-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 0;
  border-bottom: 1px solid #f1f5f9;
  gap: 8px;
}

.side-row:last-child { border-bottom: none; }
.side-label { font-size: 12px; color: #94a3b8; font-weight: 500; }
.side-value { font-size: 13px; color: #334155; font-weight: 600; text-align: right; }

.mode-badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}
.mode-live { background: #dcfce7; color: #15803d; }
.mode-mock { background: #f1f5f9; color: #475569; }

@media (max-width: 720px) {
  .layout { grid-template-columns: 1fr; }
  .side-col { order: -1; }
}
</style>
