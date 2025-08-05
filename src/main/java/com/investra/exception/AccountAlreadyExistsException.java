package com.investra.exception;

import org.springframework.http.HttpStatus;

public class AccountAlreadyExistsException extends BaseException {

    public AccountAlreadyExistsException(String message) {
        super(message, ErrorCode.ACCOUNT_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

    public AccountAlreadyExistsException(String fieldName, String fieldValue) {
        super(String.format("%s: '%s' ile kayıtlı bir hesap zaten mevcut", fieldName, fieldValue),
              ErrorCode.ACCOUNT_ALREADY_EXISTS,
              HttpStatus.CONFLICT);
    }

    public AccountAlreadyExistsException() {
        super("Bu bilgilerle kayıtlı bir hesap zaten mevcut",
              ErrorCode.ACCOUNT_ALREADY_EXISTS,
              HttpStatus.CONFLICT);
    }
}
