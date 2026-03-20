export type ServiceId = 'svc_doc_search' | 'svc_chat_assistant' | 'svc_report_generator'

export type WorkflowTemplateId = 'doc-search-report' | 'doc_search_to_report'

export type ExecutionStatus = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED'

export type ErrorCode =
  | 'INVALID_REQUEST'
  | 'POLICY_BLOCKED'
  | 'RATE_LIMIT_EXCEEDED'
  | 'AI_PROVIDER_ERROR'
  | 'INTERNAL_SERVER_ERROR'

export type CostStatus = 'pending' | 'estimated' | 'completed' | 'NOT_READY' | 'CALCULATING' | 'READY'

export type RecommendationState = 'pending' | 'available' | 'empty' | 'NONE' | 'READY'

export type ServiceExecutionMode = 'real' | 'mock'

export type ServiceCatalogItemDto = {
  service_id: ServiceId
  name: string
  category: string
  pricing_model: string
  short_description: string
  supported_models: string[]
  workflow_role: string
  tags: string[]
  recommended: boolean
  execution_mode: ServiceExecutionMode
}

export type ExampleField = {
  label: string
  value: string
}

export type ServiceResultExample = {
  headline: string
  description: string
  items: string[]
}

export type ServiceDetailDto = ServiceCatalogItemDto & {
  description: string
  input_examples: ExampleField[]
  result_example: ServiceResultExample
}

export type WorkflowStepDto = {
  order?: number
  service_id: ServiceId
  notes?: string
}

export type WorkflowStepDraft = {
  order: number
  serviceId: ServiceId
  notes: string
}

export type WorkflowDraft = {
  name: string
  steps: WorkflowStepDraft[]
  selectedTemplate: WorkflowTemplateId | null
  lastSavedWorkflowId: string | null
}

export type WorkflowDto = {
  workflow_id: string
  name: string
  steps: WorkflowStepDto[]
  created_at: string
  updated_at?: string
  selected_template?: WorkflowTemplateId
}

export type TopChunk = {
  doc_id: string
  snippet: string
  score: number
}

export type Citation = {
  doc_id: string
  title: string
  section: string
}

export type DocSearchExecutionResult = {
  type: 'doc_search'
  answer_summary: string
  top_chunks: TopChunk[]
  citations: Citation[]
  document_scope: string
}

export type ChatExecutionResult = {
  type: 'chat_assistant'
  answer: string
  bullet_points: string[]
}

export type ReportExecutionResult = {
  type: 'report_generator'
  report_text: string
  sections: string[]
  format: string
}

export type WorkflowExecutionStepResult = {
  order: number
  service_id: ServiceId
  title: string
  summary: string
  status: ExecutionStatus
  raw: Record<string, unknown>
}

export type WorkflowExecutionResult = {
  type: 'workflow'
  workflow_name: string
  step_results: WorkflowExecutionStepResult[]
  final_output: ReportExecutionResult | ChatExecutionResult | DocSearchExecutionResult
}

export type ExecutionResult =
  | DocSearchExecutionResult
  | ChatExecutionResult
  | ReportExecutionResult
  | WorkflowExecutionResult

export type AppErrorDto = {
  code: ErrorCode
  message: string
  details?: Record<string, string>
}

export type ExecutionCreateRequest = {
  service_id?: ServiceId
  workflow_id?: string
  model?: string
  input: Record<string, unknown>
}

export type ExecutionCreateResponseDto = {
  execution_id: string
  service_id?: ServiceId
  workflow_id?: string
  status: ExecutionStatus
  request_id: string
}

export type ExecutionDetailDto = {
  execution_id: string
  service_id?: ServiceId
  workflow_id?: string
  status: ExecutionStatus
  request_id: string
  input: Record<string, unknown>
  result?: ExecutionResult
  error?: AppErrorDto
  cost_status: CostStatus
  recommendation_state: RecommendationState
  recommendation_message: string
  created_at: string
  completed_at?: string
}

export type CatalogQuery = {
  keyword?: string
  category?: string
  tag?: string
}

export type CatalogServiceCardViewModel = {
  id: ServiceId
  name: string
  categoryLabel: string
  pricingLabel: string
  description: string
  modelSummary: string
  workflowRole: string
  badges: string[]
}

export type ExecutionResultViewModel = {
  title: string
  summary: string
  rawResult: string
  resultType?: ExecutionResult['type']
  requestId: string
  statusLabel: ExecutionStatus
  errorCode?: ErrorCode
}
