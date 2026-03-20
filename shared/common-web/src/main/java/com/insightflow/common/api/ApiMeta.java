package com.insightflow.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiMeta(
        @JsonProperty("request_id")
        String requestId
) {
}
