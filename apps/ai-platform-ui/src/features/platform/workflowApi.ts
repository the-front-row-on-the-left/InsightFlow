import { fetchJson, postJson } from '../../lib/api'
import { createWorkflowRecord, getWorkflowRecord } from './mockRuntime'
import type { WorkflowDto } from './types'

export type WorkflowCreateInput = {
  name: string
  steps: Array<{ service_id: string; order?: number; notes?: string }>
}

export type WorkflowApi = {
  createWorkflow(input: WorkflowCreateInput): Promise<WorkflowDto>
  getWorkflow(workflowId: string): Promise<WorkflowDto>
}

export function createMockWorkflowApi(): WorkflowApi {
  return {
    createWorkflow: createWorkflowRecord,
    getWorkflow: getWorkflowRecord,
  }
}

export function createHttpWorkflowApi(): WorkflowApi {
  return {
    async createWorkflow(input) {
      const response = await postJson<WorkflowDto, WorkflowCreateInput>('/api/workflows', input)
      return response.data
    },
    async getWorkflow(workflowId) {
      const response = await fetchJson<WorkflowDto>(`/api/workflows/${workflowId}`)
      return response.data
    },
  }
}

export const workflowApi = createMockWorkflowApi()

export async function createWorkflow(input: WorkflowCreateInput) {
  return workflowApi.createWorkflow(input)
}

export async function getWorkflow(workflowId: string) {
  return workflowApi.getWorkflow(workflowId)
}

export async function getWorkflowDetail(workflowId: string) {
  return getWorkflow(workflowId)
}

export async function saveWorkflow(
  name: string,
  steps: Array<{ serviceId: string; order: number; notes: string }>,
) {
  return createWorkflow({
    name,
    steps: steps.map((step) => ({
      service_id: step.serviceId,
      order: step.order,
      notes: step.notes,
    })),
  })
}
