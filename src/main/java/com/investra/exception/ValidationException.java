package com.investra.exception;

import org.springframework.http.HttpStatus;

/**
 * Doğrulama işlemleri sırasında oluşan hataları temsil eden özel exception sınıfı
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, ErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, cause);
    }

}
