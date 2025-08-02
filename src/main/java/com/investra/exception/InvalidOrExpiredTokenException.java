package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrExpiredTokenException extends BaseException {

    public InvalidOrExpiredTokenException(String message) {
        super(message, ErrorCode.INVALID_OR_EXPIRED_TOKEN, HttpStatus.UNAUTHORIZED);
    }

    public InvalidOrExpiredTokenException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public InvalidOrExpiredTokenException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_OR_EXPIRED_TOKEN, HttpStatus.UNAUTHORIZED, cause);
    }
}
