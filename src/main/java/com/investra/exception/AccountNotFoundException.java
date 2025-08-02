package com.investra.exception;

import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BaseException {

    public AccountNotFoundException(String message) {
        super(message, ErrorCode.ACCOUNT_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public AccountNotFoundException(Long id) {
        super(String.format("ID: '%d' olan hesap bulunamadı", id),
              ErrorCode.ACCOUNT_NOT_FOUND,
              HttpStatus.NOT_FOUND);
    }
}
