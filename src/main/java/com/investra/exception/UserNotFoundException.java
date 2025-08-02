package com.investra.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String message) {
        super(message, ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(Long id) {
        super(String.format("ID: '%d' olan kullanıcı bulunamadı", id),
              ErrorCode.USER_NOT_FOUND,
              HttpStatus.NOT_FOUND);
    }
}
