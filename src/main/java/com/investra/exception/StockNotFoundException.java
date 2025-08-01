package com.investra.exception;

import org.springframework.http.HttpStatus;

public class StockNotFoundException extends BaseException {

    public StockNotFoundException(String message) {
        super(message, ErrorCode.STOCK_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public StockNotFoundException(Long id) {
        super(String.format("ID: '%d' olan hisse senedi bulunamadı", id),
              ErrorCode.STOCK_NOT_FOUND,
              HttpStatus.NOT_FOUND);
    }
}
