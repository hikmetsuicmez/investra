package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {

    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK.getMessage(), ErrorCode.INSUFFICIENT_STOCK,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InsufficientStockException(String message) {
        super(message, ErrorCode.INSUFFICIENT_STOCK, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InsufficientStockException(Long id) {
        super(String.format("ID: '%d' olan hisse senedi için yetersiz stok", id),
                ErrorCode.INSUFFICIENT_STOCK,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
