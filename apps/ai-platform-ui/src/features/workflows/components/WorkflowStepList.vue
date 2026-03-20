<template>
  <div class="step-list">
    <article v-for="step in steps" :key="step.order" class="step-card">
      <div class="step-header">
        <div>
          <p class="step-order">Step {{ step.order }}</p>
          <h3>{{ serviceLabels[step.serviceId] }}</h3>
        </div>
        <div class="step-actions">
          <button type="button" @click="$emit('move', step.order, 'up')">위로</button>
          <button type="button" @click="$emit('move', step.order, 'down')">아래로</button>
          <button type="button" class="danger" @click="$emit('remove', step.order)">삭제</button>
        </div>
      </div>

      <p class="service-id">{{ step.serviceId }}</p>
      <textarea
        :value="step.notes"
        rows="3"
        placeholder="이 step에 대한 메모를 입력하세요."
        @input="$emit('update-notes', step.order, ($event.target as HTMLTextAreaElement).value)"
      />
    </article>
  </div>
</template>

<script setup lang="ts">
import type { ServiceId, WorkflowStepDraft } from '../../platform/types'

defineProps<{
  steps: WorkflowStepDraft[]
}>()

defineEmits<{
  remove: [order: number]
  move: [order: number, direction: 'up' | 'down']
  'update-notes': [order: number, notes: string]
}>()

const serviceLabels: Record<ServiceId, string> = {
  svc_doc_search: '문서 검색',
  svc_chat_assistant: '챗 어시스턴트',
  svc_report_generator: '보고서 생성기',
}
</script>

<style scoped>
.step-list {
  display: grid;
  gap: 12px;
}

.step-card {
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(31, 31, 31, 0.08);
}

.step-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.step-order,
.service-id {
  margin: 0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: #80592a;
}

h3 {
  margin: 6px 0 0;
  font-size: 22px;
}

.service-id {
  margin-top: 8px;
}

.step-actions {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

button,
textarea {
  font: inherit;
}

button {
  border: 0;
  border-radius: 999px;
  padding: 9px 12px;
  background: #1f1f1f;
  color: #f7f1e6;
  cursor: pointer;
}

button.danger {
  background: #a2382b;
}

textarea {
  width: 100%;
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(31, 31, 31, 0.12);
  background: #fffdf8;
  resize: vertical;
}

@media (max-width: 720px) {
  .step-header {
    flex-direction: column;
  }

  .step-actions {
    flex-wrap: wrap;
  }
}
</style>
