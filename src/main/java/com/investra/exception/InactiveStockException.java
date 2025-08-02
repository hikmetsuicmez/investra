package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InactiveStockException extends BaseException {

    public InactiveStockException(String message) {
        super(message, ErrorCode.INACTIVE_STOCK, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InactiveStockException(Long id) {
        super(String.format("ID: '%d' olan hisse senedi için yetersiz stok", id),
              ErrorCode.INACTIVE_STOCK,
              HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
