package com.investra.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.investra.exception.ErrorCode;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private int statusCode;
    private boolean success;
    private String message;
    private T data;
    private ErrorCode errorCode;
    private Integer errorCodeNumber;
    private Map<String, Serializable> meta;

    // HTTP durum koduna göre success değerini belirleyen yardımcı metot.
    public static class ResponseBuilder<T> {
        public ResponseBuilder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            this.success = (statusCode >= 200 && statusCode < 300);
            return this;
        }

        public ResponseBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public ResponseBuilder<T> errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            this.errorCodeNumber = errorCode != null ? errorCode.getCode() : null;
            return this;
        }
    }
}