<template>
  <section v-if="detail" class="detail-page">
    <ErrorBanner
      v-if="detail.error"
      :title="errorTitle"
      :message="detail.error.message"
      :request-id="detail.request_id"
      :variant="errorVariant"
    />

    <ExecutionStatusPanel
      :status="detail.status"
      :request-id="detail.request_id"
      :cost-status="detail.cost_status"
      :recommendation-message="detail.recommendation_message"
    />

    <section v-if="detail.result && isDocSearchResult(detail.result)" class="panel">
      <p class="eyebrow">RAG Result</p>
      <h2>{{ detail.result.answer_summary }}</h2>
      <p class="scope">search scope: {{ detail.result.document_scope }}</p>
      <div class="chunk-list">
        <article v-for="chunk in detail.result.top_chunks" :key="`${chunk.doc_id}-${chunk.score}`" class="chunk-card">
          <strong>{{ chunk.doc_id }}</strong>
          <p>{{ chunk.snippet }}</p>
          <span>score {{ chunk.score }}</span>
        </article>
      </div>
    </section>

    <CitationList
      v-if="detail.result && isDocSearchResult(detail.result)"
      :citations="detail.result.citations"
    />

    <section v-if="detail.result && isWorkflowResult(detail.result)" class="panel">
      <p class="eyebrow">Workflow Output</p>
      <h2>{{ detail.result.workflow_name }}</h2>
      <div class="step-results">
        <article v-for="step in detail.result.step_results" :key="step.title" class="step-card">
          <strong>{{ step.title }}</strong>
          <p>{{ step.summary }}</p>
        </article>
      </div>
      <pre>{{ JSON.stringify(detail.result.final_output, null, 2) }}</pre>
    </section>

    <section v-if="detail.result && isSimpleResult(detail.result)" class="panel">
      <p class="eyebrow">Execution Result</p>
      <h2>{{ resultTitle }}</h2>
      <pre>{{ JSON.stringify(detail.result, null, 2) }}</pre>
    </section>

    <section class="panel">
      <p class="eyebrow">Raw Result</p>
      <pre>{{ JSON.stringify(detail.result, null, 2) }}</pre>
    </section>
  </section>

  <p v-else class="empty">실행 결과를 찾을 수 없습니다.</p>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { getExecutionDetail } from '../platform/executionApi'
import type {
  DocSearchExecutionResult,
  ExecutionDetailDto,
  ExecutionResult,
  WorkflowExecutionResult,
} from '../platform/types'
import CitationList from './components/CitationList.vue'
import ErrorBanner from './components/ErrorBanner.vue'
import ExecutionStatusPanel from './components/ExecutionStatusPanel.vue'

const route = useRoute()
const detail = ref<ExecutionDetailDto | null>(null)

const resultTitle = computed(() => {
  if (!detail.value?.result) {
    return ''
  }

  if ('answer' in detail.value.result) {
    return '챗 어시스턴트 결과'
  }

  return '보고서 생성 결과'
})

const errorVariant = computed<'policy' | 'limit' | 'server'>(() => {
  if (detail.value?.error?.code === 'POLICY_BLOCKED') {
    return 'policy'
  }

  if (detail.value?.error?.code === 'RATE_LIMIT_EXCEEDED') {
    return 'limit'
  }

  return 'server'
})

const errorTitle = computed(() => {
  if (detail.value?.error?.code === 'POLICY_BLOCKED') {
    return '정책 차단'
  }

  if (detail.value?.error?.code === 'RATE_LIMIT_EXCEEDED') {
    return '요청 제한 초과'
  }

  if (detail.value?.error?.code === 'INVALID_REQUEST') {
    return '잘못된 요청'
  }

  return '실행 실패'
})

function isDocSearchResult(result: ExecutionResult): result is DocSearchExecutionResult {
  return 'answer_summary' in result
}

function isWorkflowResult(result: ExecutionResult): result is WorkflowExecutionResult {
  return 'workflow_name' in result
}

function isSimpleResult(result: ExecutionResult) {
  return !isDocSearchResult(result) && !isWorkflowResult(result)
}

async function loadExecution() {
  detail.value = await getExecutionDetail(route.params.executionId as string)
}

onMounted(loadExecution)
watch(() => route.params.executionId, loadExecution)
</script>

<style scoped>
.detail-page {
  display: grid;
  gap: 18px;
}

.panel,
.empty {
  padding: 22px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(31, 31, 31, 0.08);
  box-shadow: 0 18px 34px rgba(68, 53, 24, 0.08);
}

.eyebrow,
.scope {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #80592a;
}

h2 {
  margin: 10px 0 12px;
  font-size: clamp(24px, 3vw, 36px);
  line-height: 1.35;
}

.chunk-list,
.step-results {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.chunk-card,
.step-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(246, 240, 231, 0.9);
}

.chunk-card p,
.step-card p {
  margin: 8px 0;
  line-height: 1.5;
}

pre {
  margin: 0;
  padding: 16px;
  border-radius: 18px;
  background: #1f1f1f;
  color: #f7f1e6;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
