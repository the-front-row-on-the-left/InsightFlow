import { reactive, readonly } from 'vue'

import { readStorage, writeStorage } from '../platform/storage'
import type { ServiceId, WorkflowDraft, WorkflowStepDraft, WorkflowTemplateId } from '../platform/types'

const defaultDraft: WorkflowDraft = {
  name: '',
  steps: [],
  selectedTemplate: null,
  lastSavedWorkflowId: null,
}

const draftState = reactive<WorkflowDraft>(readStorage<WorkflowDraft>('workflow-draft', defaultDraft))

function normalizeSteps(steps: WorkflowStepDraft[]) {
  return steps.map((step, index) => ({
    ...step,
    order: index + 1,
  }))
}

function persist() {
  writeStorage('workflow-draft', draftState)
}

function setSteps(steps: WorkflowStepDraft[]) {
  draftState.steps = normalizeSteps(steps)
  persist()
}

function createStep(serviceId: ServiceId, notes = ''): WorkflowStepDraft {
  return {
    order: draftState.steps.length + 1,
    serviceId,
    notes,
  }
}

export function useWorkflowDraft() {
  function setName(name: string) {
    draftState.name = name
    persist()
  }

  function addStep(serviceId: ServiceId, notes = '') {
    draftState.steps.push(createStep(serviceId, notes))
    setSteps(draftState.steps)
  }

  function removeStep(order: number) {
    setSteps(draftState.steps.filter((step) => step.order !== order))
  }

  function moveStep(order: number, direction: 'up' | 'down') {
    const index = draftState.steps.findIndex((step) => step.order === order)

    if (index === -1) {
      return
    }

    const targetIndex = direction === 'up' ? index - 1 : index + 1

    if (targetIndex < 0 || targetIndex >= draftState.steps.length) {
      return
    }

    const next = [...draftState.steps]
    const [current] = next.splice(index, 1)
    next.splice(targetIndex, 0, current)
    setSteps(next)
  }

  function updateNotes(order: number, notes: string) {
    const next = draftState.steps.map((step) => (step.order === order ? { ...step, notes } : step))
    setSteps(next)
  }

  function applyTemplate(templateId: WorkflowTemplateId) {
    draftState.selectedTemplate = templateId

    if (templateId === 'doc_search_to_report') {
      draftState.name = draftState.name || '문서 검색 후 보고서 생성'
      setSteps([
        { order: 1, serviceId: 'svc_doc_search', notes: '검색 결과를 다음 단계 context로 사용' },
        { order: 2, serviceId: 'svc_report_generator', notes: '검색 요약과 스니펫을 바탕으로 보고서 생성' },
      ])
    }

    persist()
  }

  function clearDraft() {
    draftState.name = ''
    draftState.steps = []
    draftState.selectedTemplate = null
    draftState.lastSavedWorkflowId = null
    persist()
  }

  function setLastSavedWorkflowId(workflowId: string | null) {
    draftState.lastSavedWorkflowId = workflowId
    persist()
  }

  return {
    draft: readonly(draftState),
    setName,
    addStep,
    removeStep,
    moveStep,
    updateNotes,
    applyTemplate,
    clearDraft,
    setLastSavedWorkflowId,
  }
}
