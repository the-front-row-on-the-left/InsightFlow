import { env } from '../config/env'

export type ApiMeta = {
  request_id: string
}

export type ApiResponse<T> = {
  success: boolean
  data: T
  meta: ApiMeta
}

export const defaultHeaders = {
  'X-User-Id': env.defaultUserId,
  'X-Team-Id': env.defaultTeamId,
}

export async function fetchJson<T>(path: string, init: RequestInit = {}): Promise<ApiResponse<T>> {
  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...defaultHeaders,
      ...(init.headers ?? {}),
    },
  })

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }

  return response.json() as Promise<ApiResponse<T>>
}
