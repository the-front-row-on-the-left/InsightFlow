package com.insightflow.gateway;

import com.insightflow.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

class GatewayApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    GatewayApiException(ErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    ErrorCode errorCode() {
        return errorCode;
    }

    HttpStatus httpStatus() {
        return httpStatus;
    }
}
