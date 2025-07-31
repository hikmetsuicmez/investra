package com.investra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                exception.getHttpStatus().value(),
                exception.getErrorCode().getMessage(),
                exception.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("errorCode", exception.getErrorCode().getCode());

        return new ResponseEntity<>(errorResponse, exception.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse baseErrorResponse = new ErrorResponse(
                LocalDateTime.now(),
                400,
                ErrorCode.VALIDATION_ERROR.getMessage(),
                "Geçersiz istek içeriği",
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        ValidationErrorResponse validationErrorResponse = new ValidationErrorResponse(
                baseErrorResponse.getTimestamp(),
                baseErrorResponse.getStatus(),
                baseErrorResponse.getError(),
                baseErrorResponse.getMessage(),
                baseErrorResponse.getPath(),
                errors
        );

        return new ResponseEntity<>(validationErrorResponse, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception exception, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                500,
                "Sunucu Hatası",
                exception.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
