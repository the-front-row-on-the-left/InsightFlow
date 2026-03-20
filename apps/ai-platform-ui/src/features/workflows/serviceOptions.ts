export type WorkflowServiceOption = {
  serviceId: string
  name: string
  role: string
  summary: string
  isMock: boolean
}

export const workflowServiceOptions: WorkflowServiceOption[] = [
  {
    serviceId: 'svc_doc_search',
    name: '문서 검색',
    role: '컨텍스트 수집',
    summary: '질의와 문서 범위를 받아 근거 스니펫과 요약 응답을 반환합니다.',
    isMock: false,
  },
  {
    serviceId: 'svc_chat_assistant',
    name: '채팅 어시스턴트',
    role: '초안 작성',
    summary: '간단한 요약과 초안 생성을 위한 mock 서비스입니다.',
    isMock: true,
  },
  {
    serviceId: 'svc_report_generator',
    name: '보고서 생성',
    role: '최종 결과 생성',
    summary: '문맥을 받아 보고서 텍스트와 섹션 구조를 만드는 mock 서비스입니다.',
    isMock: true,
  },
]

export const workflowTemplateId = 'doc_search_to_report'

export function getWorkflowServiceOption(serviceId: string) {
  return workflowServiceOptions.find((service) => service.serviceId === serviceId)
}

export function getWorkflowServiceName(serviceId: string) {
  return getWorkflowServiceOption(serviceId)?.name ?? serviceId
}
