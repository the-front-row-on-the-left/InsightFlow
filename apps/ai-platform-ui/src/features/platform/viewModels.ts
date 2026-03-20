import type {
  CatalogServiceCardViewModel,
  ExecutionDetailDto,
  ExecutionResultViewModel,
  ServiceCatalogItemDto,
  WorkflowDraft,
  WorkflowStepDto,
} from './types'

export function toCatalogCards(services: ServiceCatalogItemDto[]): CatalogServiceCardViewModel[] {
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

export function toExecutionResultViewModel(detail: ExecutionDetailDto): ExecutionResultViewModel {
  if (detail.error) {
    return {
      title: '실행 실패',
      summary: detail.error.message,
      rawResult: '',
      requestId: detail.request_id,
      statusLabel: detail.status,
      errorCode: detail.error.code,
    }
  }

  if (!detail.result) {
    return {
      title: '결과 없음',
      summary: '아직 결과가 준비되지 않았습니다.',
      rawResult: '',
      requestId: detail.request_id,
      statusLabel: detail.status,
    }
  }

  if (detail.result.type === 'doc_search') {
    return {
      title: '문서 검색 결과',
      summary: detail.result.answer_summary,
      rawResult: JSON.stringify(detail.result, null, 2),
      resultType: detail.result.type,
      requestId: detail.request_id,
      statusLabel: detail.status,
    }
  }

  if (detail.result.type === 'chat_assistant') {
    return {
      title: '챗 어시스턴트 결과',
      summary: detail.result.answer,
      rawResult: JSON.stringify(detail.result, null, 2),
      resultType: detail.result.type,
      requestId: detail.request_id,
      statusLabel: detail.status,
    }
  }

  if (detail.result.type === 'report_generator') {
    return {
      title: '보고서 생성 결과',
      summary: detail.result.report_text,
      rawResult: JSON.stringify(detail.result, null, 2),
      resultType: detail.result.type,
      requestId: detail.request_id,
      statusLabel: detail.status,
    }
  }

  return {
    title: '워크플로우 실행 결과',
    summary: `${detail.result.workflow_name} 워크플로우가 ${detail.result.step_results.length}개 step으로 실행되었습니다.`,
    rawResult: JSON.stringify(detail.result, null, 2),
    resultType: detail.result.type,
    requestId: detail.request_id,
    statusLabel: detail.status,
  }
}

export function toWorkflowPayload(draft: WorkflowDraft): { name: string; steps: WorkflowStepDto[] } {
  return {
    name: draft.name.trim(),
    steps: draft.steps.map((step) => ({
      order: step.order,
      service_id: step.serviceId,
      notes: step.notes,
    })),
  }
}
