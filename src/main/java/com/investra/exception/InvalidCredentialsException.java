package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException(String message) {
        super(message, ErrorCode.INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST);
    }

    public InvalidCredentialsException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST, cause);
    }
}
