package com.investra.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, cause);
    }
}
