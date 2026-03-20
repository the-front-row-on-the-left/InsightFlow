package com.insightflow.usage.service;

import com.insightflow.usage.dto.UsageItem;
import com.insightflow.usage.dto.UsagePeriod;
import com.insightflow.usage.dto.UsageScopeResponse;
import com.insightflow.usage.dto.UsageSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsageQueryService {

    public UsageScopeResponse getUserUsage(String userId) {
        return new UsageScopeResponse(
                "user",
                userId,
                new UsagePeriod("2026-03-14", "2026-03-20", "day"),
                new UsageSummary(3, 4460, 1486, 912),
                List.of(
                        new UsageItem("req_u_001", "svc_doc_summary", "gpt-4o-mini", "SUCCEEDED", 1220, 410),
                        new UsageItem("req_u_002", "svc_doc_summary", "gpt-4o-mini", "SUCCEEDED", 1840, 365),
                        new UsageItem("req_u_003", "svc_report_generator", "gpt-4.1-mini", "FAILED", 1400, 137)
                )
        );
    }

    public UsageScopeResponse getTeamUsage(String teamId) {
        return new UsageScopeResponse(
                "team",
                teamId,
                new UsagePeriod("2026-03-14", "2026-03-20", "day"),
                new UsageSummary(9, 13840, 1538, 1284),
                List.of(
                        new UsageItem("req_t_001", "svc_doc_summary", "gpt-4o-mini", "SUCCEEDED", 1220, 410),
                        new UsageItem("req_t_002", "svc_report_generator", "gpt-4.1-mini", "SUCCEEDED", 2940, 520)
                )
        );
    }

    public UsageScopeResponse getServiceUsage(String serviceId) {
        return new UsageScopeResponse(
                "service",
                serviceId,
                new UsagePeriod("2026-03-14", "2026-03-20", "day"),
                new UsageSummary(12, 18420, 1535, 1014),
                List.of(
                        new UsageItem("req_s_001", serviceId, "gpt-4o-mini", "SUCCEEDED", 980, 240),
                        new UsageItem("req_s_002", serviceId, "gpt-4o-mini", "SUCCEEDED", 1560, 318)
                )
        );
    }
}
