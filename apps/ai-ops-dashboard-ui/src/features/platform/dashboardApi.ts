import { env } from '../../config/env'
import { fetchPlain, fetchWrapped } from '../../lib/api'
import type {
  BillingScopeResponse,
  ExecutionItem,
  InternalNotification,
  NotificationSubscription,
  OverviewData,
  RecommendationResponse,
  UsageScopeResponse,
} from './types'

export async function getUserUsage(userId = env.defaultUserId) {
  return fetchWrapped<UsageScopeResponse>(env.usageServiceBaseUrl, `/api/usage/users/${userId}`)
}

export async function getTeamUsage(teamId = env.defaultTeamId) {
  return fetchWrapped<UsageScopeResponse>(env.usageServiceBaseUrl, `/api/usage/teams/${teamId}`)
}

export async function getUserBilling(userId = env.defaultUserId) {
  return fetchWrapped<BillingScopeResponse>(env.billingServiceBaseUrl, `/api/billing/users/${userId}`)
}

export async function getTeamBilling(teamId = env.defaultTeamId) {
  return fetchWrapped<BillingScopeResponse>(env.billingServiceBaseUrl, `/api/billing/teams/${teamId}`)
}

export async function getRecommendations(userId = env.defaultUserId) {
  return fetchWrapped<RecommendationResponse>(
    env.recommendationServiceBaseUrl,
    `/api/recommendations?user_id=${encodeURIComponent(userId)}`,
  )
}

export async function getNotifications(userId = env.defaultUserId, teamId = env.defaultTeamId) {
  return fetchWrapped<InternalNotification[]>(
    env.notificationServiceBaseUrl,
    `/internal/notifications?user_id=${encodeURIComponent(userId)}&team_id=${encodeURIComponent(teamId)}`,
  )
}

export async function getNotificationSubscriptions(userId = env.defaultUserId, teamId = env.defaultTeamId) {
  return fetchWrapped<NotificationSubscription[]>(
    env.notificationServiceBaseUrl,
    `/internal/notifications/subscriptions?user_id=${encodeURIComponent(userId)}&team_id=${encodeURIComponent(teamId)}`,
  )
}

export async function getRecentExecutions() {
  try {
    const data = await fetchWrapped<ExecutionItem[]>(env.gatewayServiceBaseUrl, '/api/executions')
    const normalized = normalizeExecutions(data)
    if (normalized.length > 0) {
      return normalized
    }
  } catch {
  }

  const data = await fetchWrapped<unknown>(env.aiOpsCoreServiceBaseUrl, '/internal/executions')
  return normalizeExecutions(data)
}

export async function getOverviewData(): Promise<OverviewData> {
  const [usage, billing, recommendations, notifications, recentExecutions] = await Promise.all([
    getUserUsage(),
    getUserBilling(),
    getRecommendations(),
    getNotifications(),
    getRecentExecutions(),
  ])

  return {
    stats: {
      total_requests: usage.summary.total_requests,
      total_tokens: usage.summary.total_tokens,
      total_cost: billing.summary.total_cost,
      active_alerts: notifications.filter((item) => item.status === 'OPEN').length,
      recommendation_count: recommendations.recommendations.length,
      avg_latency_ms: usage.summary.avg_latency_ms,
    },
    recent_executions: recentExecutions.slice(0, 4),
    usage,
    billing,
    recommendations,
    notifications,
  }
}

function normalizeExecutions(input: unknown): ExecutionItem[] {
  if (Array.isArray(input)) {
    return input as ExecutionItem[]
  }

  if (
    input &&
    typeof input === 'object' &&
    'executions' in input &&
    Array.isArray((input as { executions?: unknown }).executions)
  ) {
    const records = (input as { executions: Array<Record<string, unknown>> }).executions
    return records.map((item) => {
      const result = typeof item.result === 'object' && item.result ? (item.result as Record<string, unknown>) : {}
      const error = typeof item.error === 'object' && item.error ? (item.error as Record<string, unknown>) : {}
      return {
        execution_id: String(item.execution_id ?? item.executionId ?? ''),
        request_id: String(item.request_id ?? item.requestId ?? ''),
        source: 'ai-ops-core',
        service_id: String(item.service_id ?? item.serviceId ?? ''),
        workflow_id: String(item.workflow_id ?? item.workflowId ?? ''),
        model: String(item.model ?? '-'),
        status: normalizeStatus(String(item.status ?? 'FAILED')),
        started_at: String(item.created_at ?? item.createdAt ?? new Date().toISOString()),
        finished_at: String(item.created_at ?? item.createdAt ?? new Date().toISOString()),
        duration_ms: typeof result.latency_ms === 'number' ? result.latency_ms : null,
        error_message: typeof error.message === 'string' ? error.message : null,
      }
    })
  }

  return []
}

function normalizeStatus(status: string): ExecutionItem['status'] {
  if (status.startsWith('BLOCKED')) return 'BLOCKED'
  if (status === 'RUNNING') return 'RUNNING'
  if (status === 'SUCCEEDED') return 'SUCCEEDED'
  return 'FAILED'
}
