package com.insightflow.common.web;

public record RequestContext(
        String requestId,
        String userId,
        String teamId,
        String userRole
) {
}
