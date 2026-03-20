import { env } from '../config/env'

export type ApiMeta = {
  request_id: string
}

export type ApiSuccessResponse<T> = {
  success: true
  data: T
  meta: ApiMeta
}

export type ApiErrorBody = {
  code: string
  message: string
  details?: Record<string, string>
}

export type ApiErrorResponse = {
  success: false
  error: ApiErrorBody
  meta: ApiMeta
}

export type ApiResponse<T> = ApiSuccessResponse<T> | ApiErrorResponse

export const defaultHeaders = {
  'X-User-Id': env.defaultUserId,
  'X-Team-Id': env.defaultTeamId,
}

export class ApiRequestError extends Error {
  code: string
  requestId: string
  status: number
  details?: Record<string, string>

  constructor(options: {
    code: string
    message: string
    requestId: string
    status?: number
    details?: Record<string, string>
  }) {
    super(options.message)
    this.name = 'ApiRequestError'
    this.code = options.code
    this.requestId = options.requestId
    this.status = options.status ?? 500
    this.details = options.details
  }
}

async function parseResponse<T>(response: Response): Promise<ApiResponse<T>> {
  let payload: ApiResponse<T> | null = null

  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new ApiRequestError({
        code: 'INTERNAL_SERVER_ERROR',
        message: `Request failed with status ${response.status}`,
        requestId: response.headers.get('X-Request-Id') ?? 'req_unknown',
        status: response.status,
      })
    }

    throw new ApiRequestError({
      code: 'INTERNAL_SERVER_ERROR',
      message: 'Response body could not be parsed as JSON.',
      requestId: response.headers.get('X-Request-Id') ?? 'req_unknown',
      status: response.status,
    })
  }

  if ('success' in payload && !payload.success) {
    throw new ApiRequestError({
      code: payload.error.code,
      message: payload.error.message,
      requestId: payload.meta.request_id,
      status: response.status,
      details: payload.error.details,
    })
  }

  if (!response.ok) {
    throw new ApiRequestError({
      code: 'INTERNAL_SERVER_ERROR',
      message: `Request failed with status ${response.status}`,
      requestId: 'req_unknown',
      status: response.status,
    })
  }

  return payload
}

export async function fetchJson<T>(path: string, init: RequestInit = {}): Promise<ApiSuccessResponse<T>> {
  const response = await fetch(`${env.apiBaseUrl}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...defaultHeaders,
      ...(init.headers ?? {}),
    },
  })

  return parseResponse<T>(response) as Promise<ApiSuccessResponse<T>>
}

export async function postJson<TResponse, TBody>(
  path: string,
  body: TBody,
  init: RequestInit = {},
): Promise<ApiSuccessResponse<TResponse>> {
  return fetchJson<TResponse>(path, {
    ...init,
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function createMockRequestId() {
  return `req_${crypto.randomUUID().slice(0, 8)}`
}

export function buildSuccessResponse<T>(data: T, requestId = createMockRequestId()): ApiSuccessResponse<T> {
  return {
    success: true,
    data,
    meta: {
      request_id: requestId,
    },
  }
}

export function buildErrorResponse(
  error: ApiErrorBody,
  requestId = createMockRequestId(),
): ApiErrorResponse {
  return {
    success: false,
    error,
    meta: {
      request_id: requestId,
    },
  }
}
