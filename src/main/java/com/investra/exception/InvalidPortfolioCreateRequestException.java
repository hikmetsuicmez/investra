package com.investra.exception;

import org.springframework.http.HttpStatus;

public class InvalidPortfolioCreateRequestException extends BaseException {


    public InvalidPortfolioCreateRequestException(String message) {
        super(message, ErrorCode.INVALID_PORTFOLIO_CREATE_REQUEST, HttpStatus.CONFLICT);
    }

    public InvalidPortfolioCreateRequestException(String fieldName, String fieldValue) {
        super(String.format("%s: '%s' ile kayıtlı bir hesap zaten mevcut", fieldName, fieldValue),
              ErrorCode.INVALID_PORTFOLIO_CREATE_REQUEST,
              HttpStatus.CONFLICT);
    }

    public InvalidPortfolioCreateRequestException() {
        super("Bu bilgilerle kayıtlı bir hesap zaten mevcut",
              ErrorCode.INVALID_PORTFOLIO_CREATE_REQUEST,
              HttpStatus.CONFLICT);
    }
}
