import { env } from '../../config/env'
import { fetchJson } from '../../lib/api'
import { getServiceDetailRecord, listCatalogServices as listCatalogServicesMock } from './mockRuntime'
import type { CatalogQuery, ServiceCatalogItemDto, ServiceDetailDto, ServiceId } from './types'

export type CatalogApi = {
  listServices(query?: CatalogQuery): Promise<ServiceCatalogItemDto[]>
  getServiceDetail(serviceId: ServiceId): Promise<ServiceDetailDto>
}

export function createMockCatalogApi(): CatalogApi {
  return {
    listServices: listCatalogServicesMock,
    getServiceDetail: getServiceDetailRecord,
  }
}

export function createHttpCatalogApi(): CatalogApi {
  return {
    async listServices(query = {}) {
      const searchParams = new URLSearchParams()
      if (query.keyword) {
        searchParams.set('keyword', query.keyword)
      }
      if (query.category) {
        searchParams.set('category', query.category)
      }
      if (query.tag) {
        searchParams.set('tag', query.tag)
      }

      const suffix = searchParams.toString()
      const path = suffix ? `/api/catalog/services?${suffix}` : '/api/catalog/services'
      const response = await fetchJson<ServiceCatalogItemDto[]>(path)
      return response.data
    },
    async getServiceDetail(serviceId) {
      const response = await fetchJson<ServiceDetailDto>(`/api/catalog/services/${serviceId}`)
      return response.data
    },
  }
}

export const catalogApi = env.useMockApi ? createMockCatalogApi() : createHttpCatalogApi()

export async function getCatalogServices(query?: CatalogQuery) {
  return catalogApi.listServices(query)
}

export async function getCatalogServiceDetail(serviceId: ServiceId) {
  return catalogApi.getServiceDetail(serviceId)
}

export async function listCatalogServicesApi(query?: CatalogQuery) {
  return getCatalogServices(query)
}

export async function listCatalogServices(query: string | CatalogQuery = {}) {
  if (typeof query === 'string') {
    return getCatalogServices({ keyword: query })
  }

  return getCatalogServices(query)
}
