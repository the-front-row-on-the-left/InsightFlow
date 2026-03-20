<template>
  <section class="workflow-page">
    <div class="page-header">
      <div>
        <h1>워크플로우 빌더</h1>
        <p class="subtitle">서비스를 조합해 재사용 가능한 워크플로우를 만드세요.</p>
      </div>
      <button type="button" class="btn-template" @click="applyTemplate('doc_search_to_report')">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" stroke-width="1.5"/>
          <path d="M4 7h6M7 4v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        템플릿 불러오기
      </button>
    </div>

    <div class="builder-layout">
      <!-- Left: form -->
      <div class="form-col">
        <div class="panel">
          <label class="field-label">워크플로우 이름</label>
          <input
            :value="draft.name"
            type="text"
            class="field-input"
            placeholder="예: 문서 검색 → 보고서 생성"
            @input="setName(($event.target as HTMLInputElement).value)"
          />
          <p v-if="validationMessage" class="validation-msg">{{ validationMessage }}</p>
        </div>

        <div class="panel">
          <p class="section-title">서비스 추가</p>
          <div class="service-picker">
            <button
              v-for="svc in availableServices"
              :key="svc.id"
              type="button"
              class="svc-pick-btn"
              @click="addStep(svc.id)"
            >
              <span class="svc-pick-icon">{{ svc.icon }}</span>
              <span class="svc-pick-name">{{ svc.name }}</span>
              <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                <path d="M6 2v8M2 6h8" stroke="#4f46e5" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <WorkflowStepList
          :steps="draft.steps"
          @move="moveStep"
          @remove="removeStep"
          @update-notes="updateNotes"
        />

        <div class="footer-actions">
          <button type="button" class="btn-ghost" @click="clearDraft">초기화</button>
          <button type="button" class="btn-primary" :disabled="saving || !!validationMessage" @click="handleSave">
            <span v-if="saving" class="spinner"></span>
            {{ saving ? '저장 중...' : '워크플로우 저장' }}
          </button>
        </div>

        <p v-if="savedWorkflowId" class="saved-hint">
          저장 완료 →
          <RouterLink :to="`/workflows/${savedWorkflowId}`">{{ savedWorkflowId }}</RouterLink>
        </p>
      </div>

      <!-- Right: preview -->
      <aside class="preview-col">
        <div class="panel">
          <p class="section-title">미리보기</p>
          <div v-if="draft.steps.length === 0" class="preview-empty">
            서비스를 추가하면 흐름이 여기에 표시됩니다.
          </div>
          <div v-else class="preview-flow">
            <div
              v-for="(step, i) in draft.steps"
              :key="step.order"
              class="preview-step"
            >
              <div class="preview-node">
                <span class="preview-num">{{ i + 1 }}</span>
                <span class="preview-label">{{ serviceLabel(step.serviceId) }}</span>
              </div>
              <div v-if="i < draft.steps.length - 1" class="preview-arrow">↓</div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { createWorkflow } from '../platform/workflowApi'
import type { ServiceId } from '../platform/types'
import WorkflowStepList from './components/WorkflowStepList.vue'
import { useWorkflowDraft } from './useWorkflowDraft'

const router = useRouter()
const { draft, addStep, removeStep, moveStep, updateNotes, setName, applyTemplate, clearDraft, setLastSavedWorkflowId } = useWorkflowDraft()
const saving = ref(false)

const availableServices = [
  { id: 'svc_doc_search' as ServiceId, name: '문서 검색', icon: '🔍' },
  { id: 'svc_chat_assistant' as ServiceId, name: '챗 어시스턴트', icon: '💬' },
  { id: 'svc_report_generator' as ServiceId, name: '보고서 생성기', icon: '📄' },
]

const serviceLabels: Record<ServiceId, string> = {
  svc_doc_search: '문서 검색',
  svc_chat_assistant: '챗 어시스턴트',
  svc_report_generator: '보고서 생성기',
}

function serviceLabel(id: ServiceId) { return serviceLabels[id] ?? id }

const validationMessage = computed(() => {
  if (!draft.name.trim()) return '이름을 입력해야 저장할 수 있습니다.'
  if (draft.steps.length < 2) return '2개 이상의 서비스를 추가해주세요.'
  if (draft.steps.some(s => !s.serviceId)) return '모든 스텝에 서비스가 필요합니다.'
  return ''
})

const savedWorkflowId = computed(() => draft.lastSavedWorkflowId)

async function handleSave() {
  if (validationMessage.value) return
  saving.value = true
  try {
    const workflow = await createWorkflow({
      name: draft.name.trim(),
      steps: draft.steps.map(s => ({ order: s.order, service_id: s.serviceId, notes: s.notes })),
    })
    setLastSavedWorkflowId(workflow.workflow_id)
    await router.push(`/workflows/${workflow.workflow_id}`)
  } finally { saving.value = false }
}
</script>

<style scoped>
.workflow-page { display: flex; flex-direction: column; gap: 20px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

h1 { font-size: 26px; font-weight: 800; color: #0f172a; letter-spacing: -0.03em; margin-bottom: 6px; }
.subtitle { font-size: 14px; color: #64748b; }

.btn-template {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-template:hover { border-color: #a5b4fc; color: #4f46e5; background: #f5f3ff; }

/* Layout */
.builder-layout {
  display: grid;
  grid-template-columns: 1fr 240px;
  gap: 16px;
  align-items: start;
}

.form-col { display: flex; flex-direction: column; gap: 14px; }

/* Panel */
.panel {
  padding: 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
}

.section-title { font-size: 13px; font-weight: 700; color: #0f172a; margin-bottom: 12px; }

/* Field */
.field-label { display: block; font-size: 13px; font-weight: 600; color: #374151; margin-bottom: 8px; }
.field-input {
  width: 100%;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 14px;
  color: #0f172a;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-input:focus { border-color: #a5b4fc; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); background: #fff; }
.field-input::placeholder { color: #94a3b8; }

.validation-msg { font-size: 12px; color: #f59e0b; font-weight: 500; margin-top: 8px; display: flex; align-items: center; gap: 4px; }

/* Service Picker */
.service-picker { display: flex; flex-direction: column; gap: 8px; }
.svc-pick-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
}
.svc-pick-btn:hover { border-color: #a5b4fc; background: #eff6ff; }
.svc-pick-icon { font-size: 18px; }
.svc-pick-name { flex: 1; font-size: 14px; font-weight: 600; color: #0f172a; }

/* Footer */
.footer-actions { display: flex; gap: 10px; justify-content: flex-end; }

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

.btn-ghost {
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-ghost:hover { background: #f8fafc; color: #ef4444; border-color: #fca5a5; }

.saved-hint { font-size: 13px; color: #64748b; }
.saved-hint a { color: #4f46e5; font-weight: 600; }

.spinner { width: 14px; height: 14px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Preview */
.preview-col {}
.preview-empty { font-size: 13px; color: #94a3b8; text-align: center; padding: 24px 0; line-height: 1.6; }

.preview-flow { display: flex; flex-direction: column; align-items: center; gap: 0; }

.preview-step { display: flex; flex-direction: column; align-items: center; width: 100%; }

.preview-node {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}

.preview-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #4f46e5;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.preview-label { font-size: 13px; font-weight: 600; color: #1e40af; }

.preview-arrow {
  color: #93c5fd;
  font-size: 18px;
  line-height: 1;
  padding: 4px 0;
}

@media (max-width: 720px) {
  .builder-layout { grid-template-columns: 1fr; }
  .preview-col { order: -1; }
}
</style>
