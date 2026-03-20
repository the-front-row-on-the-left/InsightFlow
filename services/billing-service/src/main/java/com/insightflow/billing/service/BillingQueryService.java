package com.insightflow.billing.service;

import com.insightflow.billing.dto.BillingItem;
import com.insightflow.billing.dto.BillingPeriod;
import com.insightflow.billing.dto.BillingScopeResponse;
import com.insightflow.billing.dto.BillingSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BillingQueryService {

    public BillingScopeResponse getUserBilling(String userId) {
        return new BillingScopeResponse(
                "user",
                userId,
                new BillingPeriod("2026-03-01", "2026-03-20", "day"),
                "KRW",
                "2026-03-v1",
                new BillingSummary("174.40", "174.40", true),
                List.of(
                        new BillingItem("req_u_001", "svc_doc_summary", "gpt-4o-mini", "174.40", "174.40")
                )
        );
    }

    public BillingScopeResponse getTeamBilling(String teamId) {
        return new BillingScopeResponse(
                "team",
                teamId,
                new BillingPeriod("2026-03-01", "2026-03-20", "day"),
                "KRW",
                "2026-03-v1",
                new BillingSummary("417.20", "417.20", true),
                List.of(
                        new BillingItem("req_t_001", "svc_doc_summary", "gpt-4o-mini", "174.40", "174.40"),
                        new BillingItem("req_t_002", "svc_report_generator", "gpt-4.1-mini", "242.80", "242.80")
                )
        );
    }

    public BillingScopeResponse getWorkflowBilling(String workflowId) {
        return new BillingScopeResponse(
                "workflow",
                workflowId,
                new BillingPeriod("2026-03-01", "2026-03-20", "day"),
                "KRW",
                "2026-03-v1",
                new BillingSummary("417.20", "417.20", true),
                List.of(
                        new BillingItem("req_w_001", "svc_doc_summary", "gpt-4o-mini", "174.40", "174.40"),
                        new BillingItem("req_w_002", "svc_report_generator", "gpt-4.1-mini", "242.80", "242.80")
                )
        );
    }
}
