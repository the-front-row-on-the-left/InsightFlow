import type { RouteRecordRaw } from 'vue-router'

import CatalogPage from '../features/catalog/CatalogPage.vue'
import ServiceDetailPage from '../features/catalog/ServiceDetailPage.vue'
import ExecutionDetailPage from '../features/executions/ExecutionDetailPage.vue'
import ExecuteServicePage from '../features/executions/ExecuteServicePage.vue'
import WorkflowBuilderPage from '../features/workflows/WorkflowBuilderPage.vue'
import WorkflowDetailPage from '../features/workflows/WorkflowDetailPage.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/catalog',
  },
  {
    path: '/catalog',
    component: CatalogPage,
  },
  {
    path: '/catalog/:serviceId',
    component: ServiceDetailPage,
  },
  {
    path: '/execute/:serviceId',
    component: ExecuteServicePage,
  },
  {
    path: '/executions/:executionId',
    component: ExecutionDetailPage,
  },
  {
    path: '/workflows/new',
    component: WorkflowBuilderPage,
  },
  {
    path: '/workflows/:workflowId',
    component: WorkflowDetailPage,
  },
]
