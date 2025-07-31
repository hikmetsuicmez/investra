package com.investra.exception;

import org.springframework.http.HttpStatus;

public class NotificationException extends BaseException {

    public NotificationException(String message) {
        super(message, ErrorCode.NOTIFICATION_ERROR, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, ErrorCode.NOTIFICATION_ERROR, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }

    public NotificationException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
