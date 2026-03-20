export type UsageSummary = {
  total_requests: number
  total_tokens: number
  avg_tokens_per_request: number
  avg_latency_ms: number
  succeeded_requests: number
  failed_requests: number
  blocked_requests: number
}

export type UsageItem = {
  request_id: string
  service_id: string
  workflow_id: string
  model: string
  status: string
  policy_result: string
  limit_result: string
  prompt_tokens: number
  completion_tokens: number
  total_tokens: number
  latency_ms: number
  requested_at: string
}

export type UsageScopeResponse = {
  scope_type: string
  scope_id: string
  period: {
    from: string
    to: string
    unit: string
  }
  summary: UsageSummary
  items: UsageItem[]
}

export type BillingSummary = {
  total_cost: string
  cost_before_rounding: string
  billable: boolean
  item_count: number
}

export type BillingItem = {
  request_id: string
  service_id: string
  model: string
  total_cost: string
  cost_before_rounding: string
  status: string
  billable: boolean
  pricing_model: string
  prompt_tokens: number
  completion_tokens: number
  total_tokens: number
  price_table_version: string
  occurred_at: string
}

export type BillingScopeResponse = {
  scope_type: string
  scope_id: string
  period: {
    from: string
    to: string
    unit: string
  }
  currency: string
  price_table_version: string
  summary: BillingSummary
  items: BillingItem[]
}

export type RecommendationItem = {
  type: string
  service_id: string
  current_model: string
  recommended_model: string
  estimated_monthly_savings: string
  estimated_token_savings: number
  confidence: 'low' | 'medium' | 'high'
  reason: string
}

export type RecommendationResponse = {
  user_id: string
  period: {
    from: string
    to: string
    unit: string
  }
  recommendations: RecommendationItem[]
}

export type NotificationSubscription = {
  event_type: string
  channel: string
  status: string
}

export type InternalNotification = {
  notification_id: string
  request_id: string
  event_type: string
  recipient_type: string
  recipient_id: string
  channel: string
  title: string
  message: string
  status: string
  occurred_at: string
  metadata: Record<string, string>
}

export type ExecutionItem = {
  execution_id: string
  request_id: string
  source: string
  service_id: string
  workflow_id: string
  model: string
  status: 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'BLOCKED'
  started_at: string
  finished_at: string | null
  duration_ms: number | null
  error_message?: string | null
}

export type OverviewStats = {
  total_requests: number
  total_tokens: number
  total_cost: string
  active_alerts: number
  recommendation_count: number
  avg_latency_ms: number
}

export type OverviewData = {
  stats: OverviewStats
  recent_executions: ExecutionItem[]
  usage: UsageScopeResponse
  billing: BillingScopeResponse
  recommendations: RecommendationResponse
  notifications: InternalNotification[]
}
