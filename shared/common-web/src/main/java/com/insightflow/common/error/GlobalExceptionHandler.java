package com.insightflow.common.error;

import com.insightflow.common.api.ApiMeta;
import com.insightflow.common.web.InsightRequestContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected server error.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode errorCode, String message) {
        ErrorResponse response = new ErrorResponse(
                false,
                new ApiError(errorCode.name(), message),
                new ApiMeta(currentRequestId())
        );
        return ResponseEntity.status(status).body(response);
    }

    private String currentRequestId() {
        return InsightRequestContextHolder.getCurrent()
                .map(context -> context.requestId())
                .orElse("unknown");
    }
}
