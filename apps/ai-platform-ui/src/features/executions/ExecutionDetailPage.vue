<template>
  <section v-if="detail" class="detail-page">
    <nav class="breadcrumb">
      <RouterLink to="/catalog">카탈로그</RouterLink>
      <span class="sep">›</span>
      <span>실행 결과</span>
    </nav>

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

    <!-- DocSearch result -->
    <template v-if="detail.result && isDocSearchResult(detail.result)">
      <div class="panel">
        <p class="panel-eyebrow">검색 결과 요약</p>
        <h2 class="panel-heading">{{ detail.result.answer_summary }}</h2>
        <span class="scope-badge">검색 범위: {{ detail.result.document_scope }}</span>

        <div class="chunk-list">
          <div
            v-for="chunk in detail.result.top_chunks"
            :key="`${chunk.doc_id}-${chunk.score}`"
            class="chunk-card"
          >
            <div class="chunk-header">
              <code class="chunk-id">{{ chunk.doc_id }}</code>
              <span class="chunk-score">점수 {{ chunk.score }}</span>
            </div>
            <p class="chunk-snippet">{{ chunk.snippet }}</p>
          </div>
        </div>
      </div>
      <CitationList :citations="detail.result.citations" />
    </template>

    <!-- Chat result -->
    <template v-else-if="detail.result && isChatResult(detail.result)">
      <div class="panel">
        <p class="panel-eyebrow">챗 어시스턴트 응답</p>
        <p class="chat-answer">{{ detail.result.answer }}</p>
        <ul v-if="detail.result.bullet_points?.length" class="bullet-list">
          <li v-for="point in detail.result.bullet_points" :key="point">{{ point }}</li>
        </ul>
      </div>
    </template>

    <!-- Report result -->
    <template v-else-if="detail.result && isReportResult(detail.result)">
      <div class="panel">
        <p class="panel-eyebrow">보고서 생성 결과</p>
        <div class="report-meta">
          <span class="format-badge">{{ formatLabel(detail.result.format) }}</span>
        </div>
        <p class="report-text">{{ detail.result.report_text }}</p>
        <div class="sections-list">
          <div
            v-for="(section, i) in detail.result.sections"
            :key="section"
            class="section-item"
          >
            <span class="section-num">{{ i + 1 }}</span>
            <span>{{ section }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Workflow result -->
    <template v-else-if="detail.result && isWorkflowResult(detail.result)">
      <div class="panel">
        <p class="panel-eyebrow">워크플로우 실행 결과</p>
        <h2 class="panel-heading">{{ detail.result.workflow_name }}</h2>
        <div class="step-results">
          <div
            v-for="step in detail.result.step_results"
            :key="step.title"
            class="step-result-card"
          >
            <span class="step-num">{{ step.order }}</span>
            <div>
              <p class="step-title">{{ step.title }}</p>
              <p class="step-summary">{{ step.summary }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </section>

  <div v-else class="empty-state">
    <p>실행 결과를 찾을 수 없습니다.</p>
    <RouterLink to="/catalog" class="btn-back">카탈로그로 돌아가기</RouterLink>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

import { getExecutionDetail } from '../platform/executionApi'
import type {
  ChatExecutionResult,
  DocSearchExecutionResult,
  ExecutionDetailDto,
  ExecutionResult,
  ReportExecutionResult,
  WorkflowExecutionResult,
} from '../platform/types'
import CitationList from './components/CitationList.vue'
import ErrorBanner from './components/ErrorBanner.vue'
import ExecutionStatusPanel from './components/ExecutionStatusPanel.vue'

const route = useRoute()
const detail = ref<ExecutionDetailDto | null>(null)

const errorVariant = computed<'policy' | 'limit' | 'server'>(() => {
  if (detail.value?.error?.code === 'POLICY_BLOCKED') return 'policy'
  if (detail.value?.error?.code === 'RATE_LIMIT_EXCEEDED') return 'limit'
  return 'server'
})

const errorTitle = computed(() => {
  const map: Record<string, string> = {
    POLICY_BLOCKED: '정책 차단',
    RATE_LIMIT_EXCEEDED: '요청 제한 초과',
    INVALID_REQUEST: '잘못된 요청',
  }
  return map[detail.value?.error?.code ?? ''] ?? '실행 실패'
})

function isDocSearchResult(r: ExecutionResult): r is DocSearchExecutionResult { return 'answer_summary' in r }
function isChatResult(r: ExecutionResult): r is ChatExecutionResult { return 'answer' in r }
function isReportResult(r: ExecutionResult): r is ReportExecutionResult { return 'report_text' in r }
function isWorkflowResult(r: ExecutionResult): r is WorkflowExecutionResult { return 'workflow_name' in r }

function formatLabel(fmt: string) {
  return fmt === 'executive_summary' ? '핵심 요약' : '상세 보고서'
}

async function loadExecution() {
  detail.value = await getExecutionDetail(route.params.executionId as string)
}

onMounted(loadExecution)
watch(() => route.params.executionId, loadExecution)
</script>

<style scoped>
.detail-page { display: flex; flex-direction: column; gap: 16px; }

.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #94a3b8; }
.breadcrumb a { color: #64748b; transition: color 0.15s; }
.breadcrumb a:hover { color: #4f46e5; }
.sep { color: #cbd5e1; }

.panel {
  padding: 24px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-eyebrow {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #4f46e5;
}

.panel-heading {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
  line-height: 1.4;
}

.scope-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 12px;
  font-weight: 600;
}

/* Chunk */
.chunk-list { display: flex; flex-direction: column; gap: 10px; }
.chunk-card {
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}
.chunk-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.chunk-id { font-family: 'SF Mono', monospace; font-size: 11px; background: #ede9fe; color: #5b21b6; padding: 2px 7px; border-radius: 4px; }
.chunk-score { font-size: 11px; font-weight: 600; color: #64748b; }
.chunk-snippet { font-size: 13px; color: #334155; line-height: 1.6; }

/* Chat */
.chat-answer { font-size: 15px; color: #1e293b; line-height: 1.7; }
.bullet-list { padding-left: 0; list-style: none; display: flex; flex-direction: column; gap: 8px; }
.bullet-list li {
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 3px solid #4f46e5;
  font-size: 14px;
  color: #334155;
}

/* Report */
.report-meta { display: flex; gap: 8px; }
.format-badge { padding: 3px 10px; border-radius: 999px; background: #ede9fe; color: #6d28d9; font-size: 12px; font-weight: 600; }
.report-text { font-size: 14px; color: #334155; line-height: 1.7; }
.sections-list { display: flex; flex-direction: column; gap: 8px; }
.section-item { display: flex; align-items: center; gap: 12px; padding: 10px 14px; background: #f8fafc; border-radius: 8px; font-size: 14px; color: #334155; font-weight: 500; }
.section-num { width: 24px; height: 24px; border-radius: 50%; background: #4f46e5; color: #fff; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }

/* Workflow */
.step-results { display: flex; flex-direction: column; gap: 10px; }
.step-result-card { display: flex; align-items: flex-start; gap: 14px; padding: 14px; background: #f8fafc; border-radius: 10px; }
.step-num { width: 26px; height: 26px; border-radius: 50%; background: #4f46e5; color: #fff; font-size: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.step-title { font-size: 14px; font-weight: 600; color: #0f172a; margin-bottom: 4px; }
.step-summary { font-size: 13px; color: #64748b; line-height: 1.5; }

/* Empty */
.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; padding: 64px; color: #94a3b8; }
.btn-back { padding: 10px 20px; border-radius: 10px; background: #4f46e5; color: #fff; font-size: 14px; font-weight: 600; }
</style>
