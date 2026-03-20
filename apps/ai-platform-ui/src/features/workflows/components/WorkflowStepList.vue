<template>
  <div class="step-list">
    <p v-if="steps.length === 0" class="empty-hint">위에서 서비스를 추가하면 스텝이 여기 쌓입니다.</p>
    <div
      v-for="step in steps"
      :key="step.order"
      class="step-card"
    >
      <div class="step-header">
        <div class="step-info">
          <span class="step-num">{{ step.order }}</span>
          <div>
            <p class="step-name">{{ serviceLabels[step.serviceId] }}</p>
            <p class="step-id">{{ step.serviceId }}</p>
          </div>
        </div>
        <div class="step-actions">
          <button type="button" class="icon-btn" title="위로" @click="$emit('move', step.order, 'up')">
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
              <path d="M6.5 10V3M3 6.5l3.5-3.5 3.5 3.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <button type="button" class="icon-btn" title="아래로" @click="$emit('move', step.order, 'down')">
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
              <path d="M6.5 3v7M10 6.5l-3.5 3.5L3 6.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <button type="button" class="icon-btn icon-btn--danger" title="삭제" @click="$emit('remove', step.order)">
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
              <path d="M3 3l7 7M10 3L3 10" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>
      <textarea
        :value="step.notes"
        class="step-notes"
        rows="2"
        placeholder="이 스텝에 대한 메모 (선택사항)"
        @input="$emit('update-notes', step.order, ($event.target as HTMLTextAreaElement).value)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ServiceId, WorkflowStepDraft } from '../../platform/types'

defineProps<{ steps: WorkflowStepDraft[] }>()
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
.step-list { display: flex; flex-direction: column; gap: 10px; }
.empty-hint { font-size: 13px; color: #94a3b8; text-align: center; padding: 16px 0; }

.step-card {
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: border-color 0.15s;
}
.step-card:hover { border-color: #c7d2fe; }

.step-header { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.step-info { display: flex; align-items: center; gap: 10px; }

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #4f46e5;
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.step-id { font-size: 11px; color: #94a3b8; font-family: 'SF Mono', monospace; }

.step-actions { display: flex; gap: 4px; }

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 7px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s;
}
.icon-btn:hover { background: #eff6ff; border-color: #a5b4fc; color: #4f46e5; }
.icon-btn--danger:hover { background: #fef2f2; border-color: #fca5a5; color: #ef4444; }

.step-notes {
  width: 100%;
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
  background: #f8fafc;
  font-size: 13px;
  color: #334155;
  resize: none;
  outline: none;
  transition: border-color 0.15s;
}
.step-notes:focus { border-color: #a5b4fc; background: #fff; }
.step-notes::placeholder { color: #cbd5e1; }
</style>
