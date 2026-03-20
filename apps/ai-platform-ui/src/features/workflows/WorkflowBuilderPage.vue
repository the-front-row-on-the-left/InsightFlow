<template>
  <section class="workflow-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">Workflow Builder</p>
        <h2>서비스 조합을 저장하고 재실행합니다.</h2>
      </div>
      <button type="button" class="secondary" @click="applyTemplate('doc_search_to_report')">
        템플릿 적용
      </button>
    </div>

    <div class="panel">
      <label class="field">
        <span>워크플로우 이름</span>
        <input :value="draft.name" type="text" placeholder="문서 검색 후 보고서 생성" @input="setName(($event.target as HTMLInputElement).value)" />
      </label>

      <div class="service-picks">
        <button type="button" @click="addStep('svc_doc_search')">문서 검색 추가</button>
        <button type="button" @click="addStep('svc_chat_assistant')">챗 어시스턴트 추가</button>
        <button type="button" @click="addStep('svc_report_generator')">보고서 생성기 추가</button>
      </div>

      <p v-if="validationMessage" class="validation">{{ validationMessage }}</p>

      <WorkflowStepList
        :steps="draft.steps"
        @move="moveStep"
        @remove="removeStep"
        @update-notes="updateNotes"
      />

      <div class="footer-actions">
        <button type="button" class="secondary" @click="clearDraft">초기화</button>
        <button type="button" :disabled="saving" @click="handleSave">
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </div>

      <p v-if="savedWorkflowId" class="saved-hint">
        마지막 저장 workflow: <RouterLink :to="`/workflows/${savedWorkflowId}`">{{ savedWorkflowId }}</RouterLink>
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { createWorkflow } from '../platform/workflowApi'
import WorkflowStepList from './components/WorkflowStepList.vue'
import { useWorkflowDraft } from './useWorkflowDraft'

const router = useRouter()
const { draft, addStep, removeStep, moveStep, updateNotes, setName, applyTemplate, clearDraft, setLastSavedWorkflowId } =
  useWorkflowDraft()

const saving = ref(false)

const validationMessage = computed(() => {
  if (!draft.name.trim()) {
    return '이름을 입력해야 저장할 수 있습니다.'
  }

  if (draft.steps.length < 2) {
    return 'step을 2개 이상 구성해야 합니다.'
  }

  if (draft.steps.some((step) => !step.serviceId)) {
    return '모든 step에는 serviceId가 필요합니다.'
  }

  return ''
})

const savedWorkflowId = computed(() => draft.lastSavedWorkflowId)

async function handleSave() {
  if (validationMessage.value) {
    return
  }

  saving.value = true

  try {
    const workflow = await createWorkflow({
      name: draft.name.trim(),
      steps: draft.steps.map((step) => ({
        order: step.order,
        service_id: step.serviceId,
        notes: step.notes,
      })),
    })
    setLastSavedWorkflowId(workflow.workflow_id)
    await router.push(`/workflows/${workflow.workflow_id}`)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.workflow-page {
  display: grid;
  gap: 18px;
}

.page-header,
.footer-actions,
.service-picks {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.panel {
  padding: 22px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
  box-shadow: 0 18px 34px rgba(68, 53, 24, 0.08);
}

.eyebrow,
.saved-hint {
  margin: 0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: #80592a;
}

h2 {
  margin: 8px 0 0;
  font-size: clamp(24px, 3vw, 34px);
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  font-weight: 600;
}

input,
button {
  font: inherit;
}

input {
  width: 100%;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(31, 31, 31, 0.12);
  background: #fffdf8;
}

button {
  border: 0;
  border-radius: 999px;
  padding: 11px 16px;
  background: #1f1f1f;
  color: #f7f1e6;
  cursor: pointer;
}

button.secondary {
  background: rgba(31, 31, 31, 0.08);
  color: #1f1f1f;
}

button:disabled {
  opacity: 0.6;
  cursor: wait;
}

.service-picks {
  margin: 18px 0;
  flex-wrap: wrap;
  justify-content: flex-start;
}

.validation {
  margin: 0 0 14px;
  color: #9f3a2c;
  font-weight: 600;
}

.footer-actions {
  margin-top: 18px;
}

.saved-hint {
  margin-top: 14px;
}

@media (max-width: 720px) {
  .page-header,
  .footer-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
