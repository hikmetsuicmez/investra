package com.investra.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
            String.format("%s, %s: '%s' değeri ile bulunamadı", resourceName, fieldName, fieldValue),
            ErrorCode.RESOURCE_NOT_FOUND,
            HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.NOT_FOUND);
    }
}
