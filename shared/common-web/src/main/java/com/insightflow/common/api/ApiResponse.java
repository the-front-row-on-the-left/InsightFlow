package com.insightflow.common.api;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiMeta meta
) {
}
