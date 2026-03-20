package com.insightflow.gateway;

import com.insightflow.common.api.ApiMeta;
import com.insightflow.common.error.ApiError;
import com.insightflow.common.error.ErrorResponse;
import com.insightflow.common.web.InsightRequestContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.insightflow.gateway")
class GatewayPlatformExceptionHandler {

    @ExceptionHandler(GatewayApiException.class)
    ResponseEntity<ErrorResponse> handleGatewayApiException(GatewayApiException exception) {
        return ResponseEntity.status(exception.httpStatus()).body(new ErrorResponse(
                false,
                new ApiError(exception.errorCode().name(), exception.getMessage()),
                new ApiMeta(currentRequestId())
        ));
    }

    private String currentRequestId() {
        return InsightRequestContextHolder.getCurrent()
                .map(context -> context.requestId())
                .orElse("unknown");
    }
}
