import { ApiRequestError, fetchJson, postJson } from '../../lib/api'
import { getMockServiceDetail } from './mockData'
import { createExecutionRecord, getExecutionRecord } from './mockRuntime'
import type {
  ExecutionCreateRequest,
  ExecutionCreateResponseDto,
  ExecutionDetailDto,
  ServiceId,
} from './types'

export type ExecutionApi = {
  createExecution(input: ExecutionCreateRequest): Promise<ExecutionCreateResponseDto>
  getExecution(executionId: string): Promise<ExecutionDetailDto>
}

export function createMockExecutionApi(): ExecutionApi {
  return {
    createExecution: createExecutionRecord,
    getExecution: getExecutionRecord,
  }
}

export function createHttpExecutionApi(): ExecutionApi {
  return {
    async createExecution(input) {
      const response = await postJson<ExecutionCreateResponseDto, ExecutionCreateRequest>(
        '/api/executions',
        input,
      )
      return response.data
    },
    async getExecution(executionId) {
      const response = await fetchJson<ExecutionDetailDto>(`/api/executions/${executionId}`)
      return response.data
    },
  }
}

export const executionApi = createMockExecutionApi()

export async function createExecution(input: ExecutionCreateRequest) {
  const response = await executionApi.createExecution(input)

  if (response.status === 'FAILED') {
    const detail = await executionApi.getExecution(response.execution_id)

    throw new ApiRequestError({
      code: detail.error?.code ?? 'INTERNAL_SERVER_ERROR',
      message: detail.error?.message ?? '실행 요청에 실패했습니다.',
      requestId: detail.request_id,
      status: 400,
      details: detail.error?.details,
    })
  }

  return response
}

export async function getExecution(executionId: string) {
  return executionApi.getExecution(executionId)
}

export async function getExecutionDetail(executionId: string) {
  return getExecution(executionId)
}

export function getDefaultModelForService(serviceId: ServiceId) {
  return getMockServiceDetail(serviceId)?.supported_models[0] ?? 'gpt-5.4-mini'
}
