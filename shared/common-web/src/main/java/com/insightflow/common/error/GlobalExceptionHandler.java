package com.insightflow.common.error;

import com.insightflow.common.api.ApiMeta;
import com.insightflow.common.web.InsightRequestContextHolder;
import com.insightflow.common.web.RequestContextFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception) {
        return build(exception.status(), exception.errorCode(), exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected server error.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode errorCode, String message) {
        String requestId = currentRequestId();
        ErrorResponse response = new ErrorResponse(
                false,
                new ApiError(errorCode.name(), message),
                new ApiMeta(requestId)
        );
        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(RequestContextFilter.REQUEST_ID_HEADER, requestId)
                .body(response);
    }

    private String currentRequestId() {
        return InsightRequestContextHolder.getCurrent()
                .map(context -> context.requestId())
                .orElse("unknown");
    }
}
