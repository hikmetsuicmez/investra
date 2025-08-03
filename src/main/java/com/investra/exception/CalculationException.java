package com.investra.exception;

import org.springframework.http.HttpStatus;

public class CalculationException extends BaseException {

    public CalculationException(String message) {
        super(message, ErrorCode.CALCULATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public CalculationException(String message, Throwable cause) {
        super(message, ErrorCode.CALCULATION_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
