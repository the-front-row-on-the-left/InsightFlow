<template>
  <section class="workflow-detail">
    <nav v-if="workflow" class="breadcrumb">
      <RouterLink to="/catalog">카탈로그</RouterLink>
      <span class="sep">›</span>
      <RouterLink to="/workflows/new">워크플로우</RouterLink>
      <span class="sep">›</span>
      <span>{{ workflow.name }}</span>
    </nav>

    <div v-if="workflow" class="content">
      <!-- Header -->
      <div class="header-panel">
        <div>
          <p class="eyebrow">저장된 워크플로우</p>
          <h1>{{ workflow.name }}</h1>
          <p class="meta">{{ workflow.workflow_id }} · {{ formattedDate }}</p>
        </div>
        <div class="header-actions">
          <RouterLink to="/workflows/new" class="btn-secondary">새로 만들기</RouterLink>
          <button type="button" class="btn-primary" :disabled="running" @click="handleRerun">
            <span v-if="running" class="spinner"></span>
            {{ running ? '실행 중...' : '다시 실행' }}
          </button>
        </div>
      </div>

      <!-- Steps -->
      <div class="steps-panel">
        <p class="section-title">스텝 구성</p>
        <div class="steps-flow">
          <div
            v-for="(step, i) in workflow.steps"
            :key="`${workflow.workflow_id}-${step.service_id}-${i}`"
            class="step-item"
          >
            <div class="step-node">
              <span class="step-num">{{ i + 1 }}</span>
              <div>
                <p class="step-name">{{ labels[step.service_id] }}</p>
                <code class="step-id">{{ step.service_id }}</code>
              </div>
            </div>
            <div v-if="i < workflow.steps.length - 1" class="step-connector">
              <div class="connector-line"></div>
              <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                <path d="M5 1l4 4-4 4M1 5h8" stroke="#a5b4fc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <p>워크플로우를 찾을 수 없습니다.</p>
      <RouterLink to="/workflows/new" class="btn-primary">새 워크플로우 만들기</RouterLink>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { createExecution } from '../platform/executionApi'
import { getWorkflow } from '../platform/workflowApi'
import type { ServiceId, WorkflowDto } from '../platform/types'

const route = useRoute()
const router = useRouter()
const workflow = ref<WorkflowDto | null>(null)
const running = ref(false)

const labels: Record<ServiceId, string> = {
  svc_doc_search: '문서 검색',
  svc_chat_assistant: '챗 어시스턴트',
  svc_report_generator: '보고서 생성기',
}

const formattedDate = computed(() =>
  workflow.value ? new Date(workflow.value.created_at).toLocaleString('ko-KR') : ''
)

async function loadWorkflow() {
  workflow.value = await getWorkflow(route.params.workflowId as string)
}

async function handleRerun() {
  if (!workflow.value) return
  running.value = true
  try {
    const execution = await createExecution({ workflow_id: workflow.value.workflow_id, input: { topic: workflow.value.name } })
    await router.push(`/executions/${execution.execution_id}`)
  } finally { running.value = false }
}

onMounted(loadWorkflow)
watch(() => route.params.workflowId, loadWorkflow)
</script>

<style scoped>
.workflow-detail { display: flex; flex-direction: column; gap: 20px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #94a3b8; }
.breadcrumb a { color: #64748b; transition: color 0.15s; }
.breadcrumb a:hover { color: #4f46e5; }
.sep { color: #cbd5e1; }

.content { display: flex; flex-direction: column; gap: 16px; }

.header-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  flex-wrap: wrap;
}

.eyebrow { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: #4f46e5; margin-bottom: 6px; }
h1 { font-size: 24px; font-weight: 800; color: #0f172a; letter-spacing: -0.02em; margin-bottom: 6px; }
.meta { font-size: 12px; color: #94a3b8; font-family: 'SF Mono', monospace; }

.header-actions { display: flex; gap: 10px; align-items: center; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
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
.btn-primary:hover:not(:disabled) { background: #4338ca; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-secondary {
  display: inline-flex;
  align-items: center;
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-secondary:hover { background: #f8fafc; border-color: #c7d2fe; }

/* Steps */
.steps-panel {
  padding: 24px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.section-title { font-size: 13px; font-weight: 700; color: #0f172a; margin-bottom: 16px; }

.steps-flow { display: flex; align-items: center; gap: 0; flex-wrap: wrap; }

.step-item { display: flex; align-items: center; }

.step-node {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.step-num {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: #4f46e5;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-name { font-size: 14px; font-weight: 600; color: #1e40af; }
.step-id { font-size: 11px; color: #93c5fd; font-family: 'SF Mono', monospace; display: block; margin-top: 2px; }

.step-connector {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
  color: #a5b4fc;
}
.connector-line { width: 16px; height: 1px; background: #bfdbfe; }

/* Empty */
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; padding: 64px; color: #94a3b8; }

.spinner { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
