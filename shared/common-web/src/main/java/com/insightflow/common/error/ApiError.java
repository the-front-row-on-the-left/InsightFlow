package com.insightflow.common.error;

public record ApiError(
        String code,
        String message
) {
}
