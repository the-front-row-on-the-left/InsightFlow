package com.insightflow.common.api;

import com.insightflow.common.web.InsightRequestContextHolder;

public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, new ApiMeta(currentRequestId()));
    }

    private static String currentRequestId() {
        return InsightRequestContextHolder.getCurrent()
                .map(context -> context.requestId())
                .orElse("unknown");
    }
}
