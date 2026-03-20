<template>
  <section class="workflow-detail">
    <div v-if="workflow" class="panel">
      <p class="eyebrow">Saved Workflow</p>
      <h2>{{ workflow.name }}</h2>
      <p class="meta">workflow_id: {{ workflow.workflow_id }} · saved {{ formattedDate }}</p>

      <ol class="step-list">
        <li v-for="(step, index) in workflow.steps" :key="`${workflow.workflow_id}-${step.service_id}-${index}`">
          <strong>{{ labels[step.service_id] }}</strong>
          <span>{{ step.service_id }}</span>
        </li>
      </ol>

      <div class="actions">
        <RouterLink class="secondary" to="/workflows/new">새 워크플로우 구성</RouterLink>
        <button type="button" :disabled="running" @click="handleRerun">
          {{ running ? '실행 중...' : '재실행' }}
        </button>
      </div>
    </div>

    <p v-else class="empty">워크플로우를 찾을 수 없습니다.</p>
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
  workflow.value ? new Date(workflow.value.created_at).toLocaleString('ko-KR') : '',
)

async function loadWorkflow() {
  const workflowId = route.params.workflowId as string
  workflow.value = await getWorkflow(workflowId)
}

async function handleRerun() {
  if (!workflow.value) {
    return
  }

  running.value = true

  try {
    const execution = await createExecution({
      workflow_id: workflow.value.workflow_id,
      input: {
        topic: workflow.value.name,
      },
    })

    await router.push(`/executions/${execution.execution_id}`)
  } finally {
    running.value = false
  }
}

onMounted(loadWorkflow)
watch(() => route.params.workflowId, loadWorkflow)
</script>

<style scoped>
.panel,
.empty {
  padding: 24px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
}

.eyebrow,
.meta {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h2 {
  margin: 10px 0;
  font-size: clamp(28px, 3.6vw, 40px);
}

.step-list {
  margin: 18px 0 0;
  padding-left: 20px;
  display: grid;
  gap: 10px;
}

li span {
  display: block;
  margin-top: 4px;
  color: #61584d;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.actions a,
.actions button {
  font: inherit;
  border: 0;
  border-radius: 999px;
  padding: 11px 16px;
  cursor: pointer;
}

.actions button {
  background: #1f1f1f;
  color: #f7f1e6;
}

.actions .secondary {
  background: rgba(31, 31, 31, 0.08);
}
</style>
