package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends BaseException {

    public InsufficientBalanceException(String message) {
        super(message, ErrorCode.INSUFFICIENT_FUNDS, HttpStatus.BAD_REQUEST);
    }

    public InsufficientBalanceException(Long id) {
        super(String.format("ID: '%d' olan hisse senedi için yetersiz stok", id),
                ErrorCode.INSUFFICIENT_FUNDS,
                HttpStatus.BAD_REQUEST);
    }

    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_FUNDS.getMessage(), ErrorCode.INSUFFICIENT_FUNDS, HttpStatus.BAD_REQUEST);
    }
}
