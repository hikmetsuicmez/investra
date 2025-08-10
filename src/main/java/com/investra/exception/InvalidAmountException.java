package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InvalidAmountException extends BaseException {

    public InvalidAmountException() {
        super(ErrorCode.INVALID_AMOUNT.getMessage(), ErrorCode.INVALID_AMOUNT, HttpStatus.BAD_REQUEST);
    }

    public InvalidAmountException(String message) {
        super(message, ErrorCode.INVALID_AMOUNT, HttpStatus.BAD_REQUEST);
    }

    public InvalidAmountException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, ErrorCode.INVALID_AMOUNT, HttpStatus.BAD_REQUEST, cause);
    }
}
