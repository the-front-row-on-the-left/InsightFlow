<template>
  <form class="execution-form" @submit.prevent="submit">
    <template v-if="serviceId === 'svc_doc_search'">
      <label class="field">
        <span>query</span>
        <textarea v-model="form.query" rows="4" placeholder="기업 문서에서 찾을 질문을 입력하세요." />
        <small v-if="inlineErrors.query">{{ inlineErrors.query }}</small>
      </label>
      <label class="field">
        <span>document_scope</span>
        <input v-model="form.document_scope" type="text" placeholder="billing, policy, ops" />
      </label>
    </template>

    <template v-else-if="serviceId === 'svc_chat_assistant'">
      <label class="field">
        <span>question</span>
        <textarea v-model="form.question" rows="4" placeholder="질문 또는 초안 요청을 입력하세요." />
        <small v-if="inlineErrors.question">{{ inlineErrors.question }}</small>
      </label>
      <label class="field">
        <span>tone</span>
        <select v-model="form.tone">
          <option value="neutral">neutral</option>
          <option value="executive">executive</option>
          <option value="friendly">friendly</option>
        </select>
      </label>
    </template>

    <template v-else>
      <label class="field">
        <span>topic</span>
        <input v-model="form.topic" type="text" placeholder="보고서 주제를 입력하세요." />
        <small v-if="inlineErrors.topic">{{ inlineErrors.topic }}</small>
      </label>
      <label class="field">
        <span>context</span>
        <textarea v-model="form.context" rows="4" placeholder="이전 단계 문맥 또는 핵심 메모를 입력하세요." />
      </label>
      <label class="field">
        <span>format</span>
        <select v-model="form.format">
          <option value="executive_summary">executive_summary</option>
          <option value="detailed_report">detailed_report</option>
        </select>
      </label>
    </template>

    <p v-if="inlineErrors.general" class="general-error">{{ inlineErrors.general }}</p>

    <button type="submit" :disabled="loading">
      {{ loading ? '실행 중...' : '실행' }}
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

const emit = defineEmits<{
  submit: [payload: Record<string, unknown>]
}>()

const form = reactive<Record<string, string>>({
  query: '',
  document_scope: 'all',
  question: '',
  tone: 'neutral',
  topic: '',
  context: '',
  format: 'executive_summary',
})

function resetForService(serviceId: ServiceId) {
  if (serviceId === 'svc_doc_search') {
    form.query = ''
    form.document_scope = 'all'
  }

  if (serviceId === 'svc_chat_assistant') {
    form.question = ''
    form.tone = 'neutral'
  }

  if (serviceId === 'svc_report_generator') {
    form.topic = ''
    form.context = ''
    form.format = 'executive_summary'
  }
}

function submit() {
  if (props.serviceId === 'svc_doc_search') {
    emit('submit', {
      query: form.query,
      document_scope: form.document_scope,
    })
    return
  }

  if (props.serviceId === 'svc_chat_assistant') {
    emit('submit', {
      question: form.question,
      tone: form.tone,
    })
    return
  }

  emit('submit', {
    topic: form.topic,
    context: form.context,
    format: form.format,
  })
}

watch(() => props.serviceId, resetForService, { immediate: true })
</script>

<style scoped>
.execution-form {
  display: grid;
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
}

.field span {
  font-weight: 600;
}

textarea,
input,
select,
button {
  font: inherit;
}

textarea,
input,
select {
  width: 100%;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(31, 31, 31, 0.12);
  background: #fffdf8;
}

small {
  color: #a2382b;
  font-weight: 600;
}

.general-error {
  margin: 0;
  color: #a2382b;
  font-weight: 700;
}

button {
  justify-self: start;
  border: 0;
  border-radius: 999px;
  padding: 11px 16px;
  background: #1f1f1f;
  color: #f7f1e6;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: wait;
}
</style>
