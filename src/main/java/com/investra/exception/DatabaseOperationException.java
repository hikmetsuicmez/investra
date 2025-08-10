package com.investra.exception;

import org.springframework.http.HttpStatus;

public class DatabaseOperationException extends BaseException {

    public DatabaseOperationException() {
        super(ErrorCode.DATABASE_ERROR.getMessage(), ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseOperationException(String message) {
        super(message, ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, ErrorCode.DATABASE_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
