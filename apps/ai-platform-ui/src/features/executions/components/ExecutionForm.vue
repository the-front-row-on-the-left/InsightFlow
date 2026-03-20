<template>
  <form class="execution-form" @submit.prevent="submit">
    <template v-if="serviceId === 'svc_doc_search'">
      <div class="field">
        <label class="field-label">검색할 내용 <span class="required">*</span></label>
        <textarea
          v-model="form.query"
          rows="4"
          class="field-input"
          :class="{ error: inlineErrors.query }"
          placeholder="예: 배포 롤백 승인 절차를 요약해줘"
        />
        <p v-if="inlineErrors.query" class="field-error">{{ inlineErrors.query }}</p>
      </div>
      <div class="field">
        <label class="field-label">검색 범위</label>
        <input v-model="form.document_scope" type="text" class="field-input" placeholder="예: billing, policy, ops" />
        <p class="field-hint">검색할 문서 카테고리를 콤마로 구분해 입력하세요. 비워두면 전체 범위를 검색합니다.</p>
      </div>
    </template>

    <template v-else-if="serviceId === 'svc_chat_assistant'">
      <div class="field">
        <label class="field-label">질문 또는 요청 <span class="required">*</span></label>
        <textarea
          v-model="form.question"
          rows="4"
          class="field-input"
          :class="{ error: inlineErrors.question }"
          placeholder="예: 이번 주 운영 리포트 초안을 작성해줘"
        />
        <p v-if="inlineErrors.question" class="field-error">{{ inlineErrors.question }}</p>
      </div>
      <div class="field">
        <label class="field-label">응답 스타일</label>
        <div class="select-wrapper">
          <select v-model="form.tone" class="field-select">
            <option value="neutral">기본</option>
            <option value="executive">경영진용 (간결·공식)</option>
            <option value="friendly">친근하게</option>
          </select>
          <svg class="select-arrow" width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="field">
        <label class="field-label">보고서 주제 <span class="required">*</span></label>
        <input
          v-model="form.topic"
          type="text"
          class="field-input"
          :class="{ error: inlineErrors.topic }"
          placeholder="예: Q2 운영 리스크 요약"
        />
        <p v-if="inlineErrors.topic" class="field-error">{{ inlineErrors.topic }}</p>
      </div>
      <div class="field">
        <label class="field-label">참고할 배경 정보</label>
        <textarea
          v-model="form.context"
          rows="4"
          class="field-input"
          placeholder="이전 단계의 결과나 추가 맥락을 입력하세요."
        />
      </div>
      <div class="field">
        <label class="field-label">보고서 형식</label>
        <div class="select-wrapper">
          <select v-model="form.format" class="field-select">
            <option value="executive_summary">핵심 요약 (Executive Summary)</option>
            <option value="detailed_report">상세 보고서</option>
          </select>
          <svg class="select-arrow" width="12" height="12" viewBox="0 0 12 12" fill="none">
            <path d="M2 4l4 4 4-4" stroke="#94a3b8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
      </div>
    </template>

    <p v-if="inlineErrors.general" class="general-error">{{ inlineErrors.general }}</p>

    <button type="submit" class="btn-submit" :disabled="loading">
      <span v-if="loading" class="spinner"></span>
      {{ loading ? '실행 중...' : '실행하기' }}
    </button>
  </form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ServiceId } from '../../platform/types'

const props = defineProps<{
  serviceId: ServiceId
  loading: boolean
  inlineErrors: Record<string, string>
}>()

const emit = defineEmits<{ submit: [payload: Record<string, unknown>] }>()

const form = reactive<Record<string, string>>({
  query: '', document_scope: 'all',
  question: '', tone: 'neutral',
  topic: '', context: '', format: 'executive_summary',
})

function resetForService(serviceId: ServiceId) {
  if (serviceId === 'svc_doc_search') { form.query = ''; form.document_scope = 'all' }
  if (serviceId === 'svc_chat_assistant') { form.question = ''; form.tone = 'neutral' }
  if (serviceId === 'svc_report_generator') { form.topic = ''; form.context = ''; form.format = 'executive_summary' }
}

function submit() {
  if (props.serviceId === 'svc_doc_search') {
    emit('submit', { query: form.query, document_scope: form.document_scope })
  } else if (props.serviceId === 'svc_chat_assistant') {
    emit('submit', { question: form.question, tone: form.tone })
  } else {
    emit('submit', { topic: form.topic, context: form.context, format: form.format })
  }
}

watch(() => props.serviceId, resetForService, { immediate: true })
</script>

<style scoped>
.execution-form { display: flex; flex-direction: column; gap: 18px; }

.field { display: flex; flex-direction: column; gap: 6px; }

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.required { color: #ef4444; }

.field-input {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 14px;
  color: #0f172a;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  resize: vertical;
}

.field-input::placeholder { color: #94a3b8; }
.field-input:focus { border-color: #a5b4fc; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
.field-input.error { border-color: #fca5a5; }
.field-input.error:focus { box-shadow: 0 0 0 3px rgba(239,68,68,0.1); }

.field-hint { font-size: 12px; color: #94a3b8; line-height: 1.5; }
.field-error { font-size: 12px; color: #ef4444; font-weight: 500; }

.select-wrapper { position: relative; }
.field-select {
  width: 100%;
  padding: 10px 36px 10px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 14px;
  color: #0f172a;
  appearance: none;
  outline: none;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field-select:focus { border-color: #a5b4fc; box-shadow: 0 0 0 3px rgba(79,70,229,0.1); }
.select-arrow { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); pointer-events: none; }

.general-error {
  padding: 10px 14px;
  border-radius: 8px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  font-size: 13px;
  font-weight: 500;
}

.btn-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  align-self: flex-end;
}
.btn-submit:hover:not(:disabled) { background: #4338ca; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
