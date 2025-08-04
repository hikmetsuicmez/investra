package com.investra.exception;

import org.springframework.http.HttpStatus;

public class ClientNotFoundException extends BaseException {

    public ClientNotFoundException(String message) {
        super(message, ErrorCode.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ClientNotFoundException(Long id) {
        super(String.format("ID: '%d' olan müşteri bulunamadı", id),
              ErrorCode.CLIENT_NOT_FOUND,
              HttpStatus.NOT_FOUND);
    }
}
