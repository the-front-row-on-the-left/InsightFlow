package com.insightflow.common.error;

import com.insightflow.common.api.ApiMeta;

public record ErrorResponse(
        boolean success,
        ApiError error,
        ApiMeta meta
) {
}
