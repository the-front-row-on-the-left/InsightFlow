import type { RouteRecordRaw } from 'vue-router'

import BillingPage from '../features/billing/BillingPage.vue'
import ExecutionsPage from '../features/executions/ExecutionsPage.vue'
import NotificationsPage from '../features/notifications/NotificationsPage.vue'
import OverviewPage from '../features/overview/OverviewPage.vue'
import RecommendationsPage from '../features/recommendations/RecommendationsPage.vue'
import UsagePage from '../features/usage/UsagePage.vue'

export const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/overview' },
  { path: '/overview', component: OverviewPage },
  { path: '/usage', component: UsagePage },
  { path: '/billing', component: BillingPage },
  { path: '/recommendations', component: RecommendationsPage },
  { path: '/notifications', component: NotificationsPage },
  { path: '/executions', component: ExecutionsPage },
]
