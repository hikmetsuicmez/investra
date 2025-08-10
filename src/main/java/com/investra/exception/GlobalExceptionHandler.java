package com.investra.exception;

import com.investra.dtos.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Response<Object>> handleBaseException(BaseException exception, WebRequest request) {
        Response<Object> errorResponse = Response.builder()
                .statusCode(exception.getHttpStatus().value())
                .message(exception.getErrorCode().getMessage())
                .errorCode(exception.getErrorCode())
                .success(false)
                .build();

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Response<Map<String, String>> validationErrorResponse = Response.<Map<String, String>>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ErrorCode.VALIDATION_ERROR.getMessage())
                .errorCode(ErrorCode.VALIDATION_ERROR)
                .success(false)
                .data(errors)
                .build();

        return new ResponseEntity<>(validationErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Object>> handleAccessDeniedException(AccessDeniedException exception,
            WebRequest request) {
        Response<Object> errorResponse = Response.builder()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message(ErrorCode.FORBIDDEN.getMessage())
                .errorCode(ErrorCode.FORBIDDEN)
                .success(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response<Object>> handleAuthenticationException(AuthenticationException exception,
            WebRequest request) {
        Response<Object> errorResponse = Response.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .message(ErrorCode.UNAUTHORIZED.getMessage())
                .errorCode(ErrorCode.UNAUTHORIZED)
                .success(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Object>> handleGlobalException(Exception exception, WebRequest request) {
        Response<Object> errorResponse = Response.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ErrorCode.INTERNAL_ERROR.getMessage())
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .success(false)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
