export const env = {
  usageServiceBaseUrl: import.meta.env.VITE_USAGE_SERVICE_BASE_URL ?? 'http://localhost:8083',
  billingServiceBaseUrl: import.meta.env.VITE_BILLING_SERVICE_BASE_URL ?? 'http://localhost:8084',
  notificationServiceBaseUrl: import.meta.env.VITE_NOTIFICATION_SERVICE_BASE_URL ?? 'http://localhost:8085',
  recommendationServiceBaseUrl: import.meta.env.VITE_RECOMMENDATION_SERVICE_BASE_URL ?? 'http://localhost:8086',
  aiOpsCoreServiceBaseUrl: import.meta.env.VITE_AI_OPS_CORE_SERVICE_BASE_URL ?? 'http://localhost:8087',
  gatewayServiceBaseUrl: import.meta.env.VITE_GATEWAY_SERVICE_BASE_URL ?? 'http://localhost:8080',
  defaultUserId: import.meta.env.VITE_DEFAULT_USER_ID ?? 'u_demo_001',
  defaultTeamId: import.meta.env.VITE_DEFAULT_TEAM_ID ?? 't_demo',
}
