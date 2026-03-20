import type {
  CatalogServiceCardViewModel,
  ChatExecutionResult,
  Citation,
  DocSearchExecutionResult,
  ReportExecutionResult,
  ServiceCatalogItemDto,
  ServiceDetailDto,
  ServiceId,
  TopChunk,
  WorkflowDto,
  WorkflowStepDto,
} from './types'

export const catalogServices: ServiceDetailDto[] = [
  {
    service_id: 'svc_doc_search',
    name: '문서 검색',
    category: 'Knowledge Retrieval',
    pricing_model: 'per_request + token_estimate',
    short_description: '기업 문서에서 근거를 찾아 요약 응답을 반환합니다.',
    supported_models: ['gpt-5.4-mini', 'rag-hybrid-mock'],
    workflow_role: '첫 단계 컨텍스트 수집',
    tags: ['rag', 'citation', 'search'],
    recommended: true,
    execution_mode: 'real',
    description:
      '사내 정책, 운영 가이드, 회의록에서 관련 근거를 찾고 answer_summary, top_chunks, citations를 함께 반환합니다.',
    input_examples: [
      { label: 'query', value: '배포 롤백 승인 절차를 요약해줘' },
      { label: 'document_scope', value: 'ops-handbook' },
    ],
    result_example: {
      headline: '근거 기반 검색 결과',
      description: '요약 답변과 근거 스니펫, citation 목록을 함께 반환합니다.',
      items: ['answer_summary', 'top_chunks', 'citations'],
    },
  },
  {
    service_id: 'svc_chat_assistant',
    name: '챗 어시스턴트',
    category: 'Conversation',
    pricing_model: 'per_request',
    short_description: '간단한 질의응답, 요약, 초안 생성을 mock 결과로 제공합니다.',
    supported_models: ['gpt-5.4-mini'],
    workflow_role: '중간 초안 생성 후보',
    tags: ['chat', 'summary', 'mock'],
    recommended: false,
    execution_mode: 'mock',
    description:
      '짧은 질문 답변과 항목 요약을 빠르게 시연하는 mock assistant입니다. UI 흐름 검증용으로 고정 결과를 돌려줍니다.',
    input_examples: [
      { label: 'question', value: '이번 주 운영 리포트 초안을 작성해줘' },
      { label: 'tone', value: 'executive' },
    ],
    result_example: {
      headline: 'Mock assistant 응답',
      description: 'answer와 bullet_points를 고정된 형태로 반환합니다.',
      items: ['answer', 'bullet_points'],
    },
  },
  {
    service_id: 'svc_report_generator',
    name: '보고서 생성기',
    category: 'Reporting',
    pricing_model: 'per_report',
    short_description: '검색 결과나 입력 문맥을 바탕으로 보고서를 생성합니다.',
    supported_models: ['gpt-5.4-mini'],
    workflow_role: '마지막 단계 결과 생성',
    tags: ['report', 'workflow', 'mock'],
    recommended: true,
    execution_mode: 'mock',
    description:
      '주제와 context를 받아 brief report 형태의 mock 결과를 생성합니다. 워크플로우 최종 step 시연용입니다.',
    input_examples: [
      { label: 'topic', value: 'Q2 운영 리스크 요약' },
      { label: 'format', value: 'briefing' },
    ],
    result_example: {
      headline: 'Mock report 결과',
      description: 'report_text와 sections를 반환하는 고정 결과입니다.',
      items: ['report_text', 'sections'],
    },
  },
]

export const catalogIndex: ServiceCatalogItemDto[] = catalogServices.map(
  ({ description, input_examples, result_example, ...service }) => ({
    ...service,
  }),
)

const baseChunks: TopChunk[] = [
  {
    doc_id: 'doc_ops_001',
    snippet: '롤백 전 서비스 오너 승인과 영향 범위 확인이 필요하다.',
    score: 0.93,
  },
  {
    doc_id: 'doc_ops_014',
    snippet: '15분 내 복구가 불가하면 즉시 롤백하고 변경 기록을 남긴다.',
    score: 0.9,
  },
  {
    doc_id: 'doc_ops_021',
    snippet: '롤백 이후 SRE 채널에 결과를 보고하고 재배포 윈도우를 재협의한다.',
    score: 0.87,
  },
]

const baseCitations: Citation[] = [
  { doc_id: 'doc_ops_001', title: 'Operations Handbook', section: '3.2 Rollback Approval' },
  { doc_id: 'doc_ops_014', title: 'Deployment Runbook', section: '5.1 Recovery Window' },
  { doc_id: 'doc_ops_021', title: 'SRE Escalation Guide', section: '2.4 Reporting' },
]

export const defaultWorkflowTemplate: WorkflowDto = {
  workflow_id: 'wf_template_doc_report',
  name: '문서 검색 후 보고서 생성',
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
  selected_template: 'doc_search_to_report',
  steps: [
    { order: 1, service_id: 'svc_doc_search', notes: '검색 결과에서 핵심 근거를 수집합니다.' },
    { order: 2, service_id: 'svc_report_generator', notes: '검색 결과를 보고서로 정리합니다.' },
  ],
}

export function listMockServices(): ServiceCatalogItemDto[] {
  return catalogIndex.map((service) => ({
    ...service,
    supported_models: [...service.supported_models],
    tags: [...service.tags],
  }))
}

export function getMockServiceDetail(serviceId: ServiceId): ServiceDetailDto | undefined {
  const service = catalogServices.find((item) => item.service_id === serviceId)
  if (!service) {
    return undefined
  }

  return {
    ...service,
    supported_models: [...service.supported_models],
    tags: [...service.tags],
    input_examples: service.input_examples.map((example) => ({ ...example })),
    result_example: {
      ...service.result_example,
      items: [...service.result_example.items],
    },
  }
}

export function buildDocSearchResult(query: string, scope: string): DocSearchExecutionResult {
  return {
    type: 'doc_search',
    answer_summary: `문서 검색 결과: "${query}"와 관련된 근거를 찾았고, ${scope} 범위 기준으로 핵심 절차를 요약했습니다.`,
    top_chunks: baseChunks.map((chunk) => ({ ...chunk })),
    citations: baseCitations.map((citation) => ({ ...citation })),
    document_scope: scope,
  }
}

export function buildChatResult(question: string): ChatExecutionResult {
  return {
    type: 'chat_assistant',
    answer: `"${question}" 요청에 대한 mock assistant 응답입니다. 핵심만 간결하게 정리했습니다.`,
    bullet_points: [
      '핵심 지표 변화 요약',
      '이번 주 이슈와 대응 포인트',
      '다음 주 실행 권고안',
    ],
  }
}

export function buildReportResult(topic: string, context: string, format = 'briefing'): ReportExecutionResult {
  return {
    type: 'report_generator',
    report_text: `${topic} 보고서 초안입니다. ${context}를 바탕으로 현황, 리스크, 권고사항을 정리했습니다.`,
    sections: ['배경', '핵심 발견', '리스크', '권고안'],
    format,
  }
}

export function normalizeWorkflowSteps(steps: WorkflowStepDto[]): WorkflowStepDto[] {
  return steps.map((step, index) => ({
    order: step.order ?? index + 1,
    service_id: step.service_id,
    notes: step.notes ?? '',
  }))
}

export function buildServiceCards(services: ServiceCatalogItemDto[]): CatalogServiceCardViewModel[] {
  return services.map((service) => ({
    id: service.service_id,
    name: service.name,
    categoryLabel: service.category,
    pricingLabel: service.pricing_model,
    description: service.short_description,
    modelSummary: service.supported_models.join(', '),
    workflowRole: service.workflow_role,
    badges: [
      service.execution_mode === 'real' ? 'Live Demo' : 'Mock',
      ...(service.recommended ? ['Recommended'] : []),
    ],
  }))
}
