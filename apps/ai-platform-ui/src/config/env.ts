export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  defaultUserId: import.meta.env.VITE_DEFAULT_USER_ID ?? 'u_demo_001',
  defaultTeamId: import.meta.env.VITE_DEFAULT_TEAM_ID ?? 't_demo',
  useMockApi: (import.meta.env.VITE_USE_MOCK_API ?? 'false') === 'true',
}
