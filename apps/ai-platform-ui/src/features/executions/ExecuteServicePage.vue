<template>
  <section v-if="service" class="execute-page">
    <div class="hero">
      <div>
        <p class="eyebrow">Execute Service</p>
        <h2>{{ service.name }}</h2>
        <p class="description">{{ service.short_description }}</p>
      </div>
      <RouterLink class="ghost-link" :to="`/catalog/${service.service_id}`">상세 보기</RouterLink>
    </div>

    <ErrorBanner
      v-if="bannerError"
      :title="bannerError.title"
      :message="bannerError.message"
      :request-id="bannerError.requestId"
      :variant="bannerError.variant"
    />

    <section class="panel">
      <ExecutionForm :service-id="service.service_id" :loading="submitting" :inline-errors="inlineErrors" @submit="handleSubmit" />
    </section>
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
const bannerError = ref<null | {
  title: string
  message: string
  requestId: string
  variant: 'policy' | 'limit' | 'server'
}>(null)

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
    const execution = await createExecution({
      service_id: serviceId.value,
      model: service.value?.supported_models[0] ?? 'gpt-5.4-mini',
      input,
    })

    if (execution.status === 'FAILED') {
      const failedExecution = await getExecutionDetail(execution.execution_id)

      if (failedExecution.error?.code === 'INVALID_REQUEST') {
        inlineErrors.value = failedExecution.error.details ?? { general: failedExecution.error.message }
      } else if (failedExecution.error?.code === 'POLICY_BLOCKED') {
        bannerError.value = {
          title: '정책 차단',
          message: failedExecution.error.message,
          requestId: failedExecution.request_id,
          variant: 'policy',
        }
      } else if (failedExecution.error?.code === 'RATE_LIMIT_EXCEEDED') {
        bannerError.value = {
          title: '요청 제한 초과',
          message: failedExecution.error.message,
          requestId: failedExecution.request_id,
          variant: 'limit',
        }
      } else {
        bannerError.value = {
          title: '실행 실패',
          message: failedExecution.error?.message ?? '알 수 없는 오류가 발생했습니다.',
          requestId: failedExecution.request_id,
          variant: 'server',
        }
      }

      return
    }

    await router.push(`/executions/${execution.execution_id}`)
  } catch (error) {
    if (error instanceof ApiRequestError) {
      if (error.code === 'INVALID_REQUEST') {
        inlineErrors.value = error.details ?? {}
      } else if (error.code === 'POLICY_BLOCKED') {
        bannerError.value = {
          title: '정책 차단',
          message: error.message,
          requestId: error.requestId,
          variant: 'policy',
        }
      } else if (error.code === 'RATE_LIMIT_EXCEEDED') {
        bannerError.value = {
          title: '요청 제한 초과',
          message: error.message,
          requestId: error.requestId,
          variant: 'limit',
        }
      } else {
        bannerError.value = {
          title: '실행 실패',
          message: error.message,
          requestId: error.requestId,
          variant: 'server',
        }
      }
    } else {
      bannerError.value = {
        title: '실행 실패',
        message: '알 수 없는 오류가 발생했습니다.',
        requestId: 'req_unknown',
        variant: 'server',
      }
    }
  } finally {
    submitting.value = false
  }
}

onMounted(loadService)
watch(serviceId, loadService)
</script>

<style scoped>
.execute-page {
  display: grid;
  gap: 18px;
}

.hero,
.panel {
  padding: 22px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
  box-shadow: 0 18px 34px rgba(68, 53, 24, 0.08);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h2 {
  margin: 8px 0;
  font-size: clamp(30px, 4vw, 46px);
}

.description {
  margin: 0;
  max-width: 680px;
  color: #514a40;
}

.ghost-link {
  align-self: start;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(31, 31, 31, 0.08);
}

@media (max-width: 720px) {
  .hero {
    flex-direction: column;
  }
}
</style>
