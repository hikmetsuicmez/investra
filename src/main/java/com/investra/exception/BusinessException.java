package com.investra.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, ErrorCode.OPERATION_FAILED, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, ErrorCode.OPERATION_FAILED, HttpStatus.BAD_REQUEST, cause);
    }
}
