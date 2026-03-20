import { ApiRequestError, createMockRequestId } from '../../lib/api'
import { buildDocSearchResult, buildChatResult, buildReportResult, defaultWorkflowTemplate, getMockServiceDetail, listMockServices, normalizeWorkflowSteps } from './mockData'
import { readStorage, writeStorage } from './storage'
import type {
  AppErrorDto,
  CatalogQuery,
  ErrorCode,
  ExecutionCreateRequest,
  ExecutionCreateResponseDto,
  ExecutionDetailDto,
  ExecutionResult,
  ExecutionStatus,
  ServiceCatalogItemDto,
  ServiceDetailDto,
  ServiceId,
  WorkflowDto,
  WorkflowExecutionResult,
  WorkflowStepDto,
} from './types'

const executionStorageKey = 'executions'
const workflowStorageKey = 'workflows'

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

function now() {
  return new Date().toISOString()
}

function normalizeText(value: unknown): string {
  return typeof value === 'string' ? value.trim().toLowerCase() : ''
}

function ensureServiceId(value: string): value is ServiceId {
  return value === 'svc_doc_search' || value === 'svc_chat_assistant' || value === 'svc_report_generator'
}

function nextId(prefix: string): string {
  return `${prefix}_${crypto.randomUUID().slice(0, 8)}`
}

function delay<T>(value: T): Promise<T> {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(value), 120)
  })
}

function readWorkflows(): WorkflowDto[] {
  const stored = readStorage<WorkflowDto[]>(workflowStorageKey, [defaultWorkflowTemplate])
  return stored.length > 0 ? stored : [defaultWorkflowTemplate]
}

function writeWorkflows(workflows: WorkflowDto[]) {
  writeStorage(workflowStorageKey, workflows)
}

function readExecutions(): Record<string, ExecutionDetailDto> {
  return readStorage<Record<string, ExecutionDetailDto>>(executionStorageKey, {})
}

function writeExecutions(executions: Record<string, ExecutionDetailDto>) {
  writeStorage(executionStorageKey, executions)
}

function findErrorTrigger(input: Record<string, unknown>): ErrorCode | undefined {
  const textValues = Object.values(input).flatMap((value) => {
    if (typeof value === 'string') {
      return [value.toLowerCase()]
    }
    if (Array.isArray(value)) {
      return value.filter((item): item is string => typeof item === 'string').map((item) => item.toLowerCase())
    }
    return []
  })

  if (textValues.some((value) => value.includes('policy_block'))) {
    return 'POLICY_BLOCKED'
  }
  if (textValues.some((value) => value.includes('rate_limit'))) {
    return 'RATE_LIMIT_EXCEEDED'
  }
  if (textValues.some((value) => value.includes('provider_error'))) {
    return 'AI_PROVIDER_ERROR'
  }
  if (textValues.some((value) => value.includes('invalid'))) {
    return 'INVALID_REQUEST'
  }
  return undefined
}

function buildError(
  code: ErrorCode,
  message?: string,
  details?: Record<string, string>,
): AppErrorDto {
  switch (code) {
    case 'INVALID_REQUEST':
      return {
        code,
        message: message ?? '입력값이 누락되었거나 형식이 올바르지 않습니다.',
        details,
      }
    case 'POLICY_BLOCKED':
      return { code, message: '정책에 의해 요청이 차단되었습니다.', details }
    case 'RATE_LIMIT_EXCEEDED':
      return { code, message: '요청 제한을 초과했습니다. 잠시 후 다시 시도해 주세요.', details }
    case 'AI_PROVIDER_ERROR':
      return { code, message: 'AI 제공자 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.', details }
    default:
      return { code, message: '일시적인 서버 오류가 발생했습니다.', details }
  }
}

function buildExecutionFailure(
  requestId: string,
  executionId: string,
  request: ExecutionCreateRequest,
  error: AppErrorDto,
): ExecutionDetailDto {
  return {
    execution_id: executionId,
    service_id: request.service_id,
    workflow_id: request.workflow_id,
    status: 'FAILED',
    request_id: requestId,
    input: clone(request.input),
    error,
    cost_status: 'pending',
    recommendation_state: 'empty',
    recommendation_message: '실패 실행에는 추천을 제공하지 않습니다.',
    created_at: now(),
    completed_at: now(),
  }
}

function normalizeWorkflow(workflow: WorkflowDto): WorkflowDto {
  return {
    ...workflow,
    steps: normalizeWorkflowSteps(workflow.steps),
  }
}

function executeSingleService(serviceId: ServiceId, input: Record<string, unknown>): ExecutionResult {
  if (serviceId === 'svc_doc_search') {
    return buildDocSearchResult(
      normalizeText(input.query) || '기본 질의',
      normalizeText(input.document_scope) || 'all-docs',
    )
  }

  if (serviceId === 'svc_chat_assistant') {
    return buildChatResult(normalizeText(input.question) || '기본 질문')
  }

  return buildReportResult(
    normalizeText(input.topic) || '기본 보고서 주제',
    normalizeText(input.context) || '문서 검색 요약 결과',
    typeof input.format === 'string' ? input.format : 'briefing',
  )
}

function executeWorkflow(workflow: WorkflowDto, input: Record<string, unknown>): WorkflowExecutionResult {
  const stepResults = normalizeWorkflowSteps(workflow.steps).map((step) => {
    let raw: ExecutionResult

    if (step.service_id === 'svc_doc_search') {
      raw = buildDocSearchResult(
        normalizeText(input.query) || `${workflow.name} 관련 핵심 문서를 찾아줘`,
        normalizeText(input.document_scope) || 'all-docs',
      )
    } else if (step.service_id === 'svc_chat_assistant') {
      raw = buildChatResult(normalizeText(input.question) || workflow.name)
    } else {
      raw = buildReportResult(
        normalizeText(input.topic) || workflow.name,
        typeof input.context === 'string'
          ? input.context
          : '문서 검색 요약과 top_chunks를 바탕으로 생성',
        typeof input.format === 'string' ? input.format : 'briefing',
      )
    }

    const summary =
      raw.type === 'doc_search'
        ? raw.answer_summary
        : raw.type === 'chat_assistant'
          ? raw.answer
          : raw.report_text

    return {
      order: step.order ?? 1,
      service_id: step.service_id,
      title: getMockServiceDetail(step.service_id)?.name ?? step.service_id,
      summary,
      status: 'SUCCEEDED' as ExecutionStatus,
      raw: raw as unknown as Record<string, unknown>,
    }
  })

  const finalRaw = stepResults[stepResults.length - 1]?.raw as
    | WorkflowExecutionResult['final_output']
    | undefined

  return {
    type: 'workflow',
    workflow_name: workflow.name,
    step_results: stepResults,
    final_output:
      finalRaw && 'type' in finalRaw
        ? finalRaw
        : buildReportResult(workflow.name, '워크플로우 기본 결과'),
  }
}

function validateRequest(request: ExecutionCreateRequest): AppErrorDto | undefined {
  const trigger = findErrorTrigger(request.input)
  if (trigger) {
    return buildError(trigger)
  }

  if (!request.service_id && !request.workflow_id) {
    return buildError('INVALID_REQUEST', 'service_id 또는 workflow_id 중 하나는 필요합니다.', {
      general: '실행 대상 서비스 또는 워크플로우를 선택하세요.',
    })
  }

  if (request.service_id === 'svc_doc_search' && !normalizeText(request.input.query)) {
    return buildError('INVALID_REQUEST', '문서 검색에는 query 입력이 필요합니다.', {
      query: 'query를 입력하세요.',
    })
  }
  if (request.service_id === 'svc_chat_assistant' && !normalizeText(request.input.question)) {
    return buildError('INVALID_REQUEST', '챗 어시스턴트에는 question 입력이 필요합니다.', {
      question: 'question을 입력하세요.',
    })
  }
  if (request.service_id === 'svc_report_generator' && !normalizeText(request.input.topic)) {
    return buildError('INVALID_REQUEST', '보고서 생성에는 topic 입력이 필요합니다.', {
      topic: 'topic을 입력하세요.',
    })
  }

  return undefined
}

function saveExecution(detail: ExecutionDetailDto) {
  const executions = readExecutions()
  executions[detail.execution_id] = detail
  writeExecutions(executions)
}

export async function listCatalogServices(query: CatalogQuery = {}): Promise<ServiceCatalogItemDto[]> {
  const keyword = normalizeText(query.keyword)
  const category = normalizeText(query.category)
  const tag = normalizeText(query.tag)

  const services = listMockServices().filter((service) => {
    const matchesKeyword =
      !keyword ||
      [service.name, service.short_description, service.service_id, ...service.tags]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    const matchesCategory = !category || service.category.toLowerCase().includes(category)
    const matchesTag = !tag || service.tags.some((item) => item.toLowerCase().includes(tag))
    return matchesKeyword && matchesCategory && matchesTag
  })

  return delay(services)
}

export async function getServiceDetailRecord(serviceId: ServiceId): Promise<ServiceDetailDto> {
  const detail = getMockServiceDetail(serviceId)

  if (!detail) {
    throw new ApiRequestError({
      code: 'INVALID_REQUEST',
      message: '서비스 정보를 찾을 수 없습니다.',
      requestId: createMockRequestId(),
      status: 404,
    })
  }

  return delay(detail)
}

export async function createWorkflowRecord(input: {
  name: string
  steps: Array<{ service_id: string; order?: number; notes?: string }>
}): Promise<WorkflowDto> {
  if (!input.name.trim() || input.steps.length < 2) {
    throw new ApiRequestError({
      code: 'INVALID_REQUEST',
      message: '워크플로우 이름과 2개 이상의 step이 필요합니다.',
      requestId: createMockRequestId(),
      status: 400,
    })
  }

  const normalizedSteps: WorkflowStepDto[] = input.steps.map((step, index) => {
    if (!ensureServiceId(step.service_id)) {
      throw new ApiRequestError({
        code: 'INVALID_REQUEST',
        message: '유효하지 않은 service_id가 포함되어 있습니다.',
        requestId: createMockRequestId(),
        status: 400,
      })
    }

    return {
      order: step.order ?? index + 1,
      service_id: step.service_id,
      notes: step.notes ?? '',
    }
  })

  const timestamp = now()
  const workflow: WorkflowDto = {
    workflow_id: nextId('wf'),
    name: input.name.trim(),
    steps: normalizedSteps,
    created_at: timestamp,
    updated_at: timestamp,
    selected_template:
      normalizedSteps.length === 2 &&
      normalizedSteps[0]?.service_id === 'svc_doc_search' &&
      normalizedSteps[1]?.service_id === 'svc_report_generator'
        ? 'doc_search_to_report'
        : undefined,
  }

  const workflows = readWorkflows().filter((item) => item.workflow_id !== workflow.workflow_id)
  workflows.unshift(workflow)
  writeWorkflows(workflows)

  return delay(clone(workflow))
}

export async function getWorkflowRecord(workflowId: string): Promise<WorkflowDto> {
  const workflow = readWorkflows().find((item) => item.workflow_id === workflowId)

  if (!workflow) {
    throw new ApiRequestError({
      code: 'INVALID_REQUEST',
      message: '워크플로우를 찾을 수 없습니다.',
      requestId: createMockRequestId(),
      status: 404,
    })
  }

  return delay(clone(normalizeWorkflow(workflow)))
}

export async function createExecutionRecord(
  request: ExecutionCreateRequest,
): Promise<ExecutionCreateResponseDto> {
  const requestId = createMockRequestId()
  const executionId = nextId('exe')
  const validationError = validateRequest(request)

  if (validationError) {
    const failedDetail = buildExecutionFailure(requestId, executionId, request, validationError)
    saveExecution(failedDetail)
    return delay({
      execution_id: executionId,
      service_id: request.service_id,
      workflow_id: request.workflow_id,
      status: failedDetail.status,
      request_id: requestId,
    })
  }

  let result: ExecutionResult
  let recommendationState: ExecutionDetailDto['recommendation_state'] = 'pending'
  let recommendationMessage = '추천을 계산 중입니다.'
  let costStatus: ExecutionDetailDto['cost_status'] = 'pending'

  if (request.workflow_id) {
    const workflow = readWorkflows().find((item) => item.workflow_id === request.workflow_id)

    if (!workflow) {
      throw new ApiRequestError({
        code: 'INVALID_REQUEST',
        message: '워크플로우를 찾을 수 없습니다.',
        requestId,
        status: 404,
      })
    }

    result = executeWorkflow(workflow, request.input)
    recommendationState = 'available'
    recommendationMessage = '문서 검색 결과를 기반으로 보고서 생성 step을 추천합니다.'
    costStatus = 'estimated'
  } else {
    result = executeSingleService(request.service_id as ServiceId, request.input)
    if (request.service_id === 'svc_doc_search') {
      recommendationState = 'available'
      recommendationMessage = '다음 단계로 svc_report_generator 조합을 권장합니다.'
      costStatus = 'estimated'
    }
  }

  const detail: ExecutionDetailDto = {
    execution_id: executionId,
    service_id: request.service_id,
    workflow_id: request.workflow_id,
    status: 'SUCCEEDED',
    request_id: requestId,
    input: clone(request.input),
    result,
    cost_status: costStatus,
    recommendation_state: recommendationState,
    recommendation_message: recommendationMessage,
    created_at: now(),
    completed_at: now(),
  }

  saveExecution(detail)

  return delay({
    execution_id: executionId,
    service_id: request.service_id,
    workflow_id: request.workflow_id,
    status: detail.status,
    request_id: requestId,
  })
}

export async function getExecutionRecord(executionId: string): Promise<ExecutionDetailDto> {
  const execution = readExecutions()[executionId]

  if (!execution) {
    throw new ApiRequestError({
      code: 'INVALID_REQUEST',
      message: '실행 결과를 찾을 수 없습니다.',
      requestId: createMockRequestId(),
      status: 404,
    })
  }

  return delay(clone(execution))
}
