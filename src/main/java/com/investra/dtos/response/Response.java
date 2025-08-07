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

    @JsonProperty("success")  // JSON'da "success" olarak görünsün
    private boolean isSuccess;

    private String message;
    private T data;
    private ErrorCode errorCode;
    private Map<String, Serializable> meta;

    // HTTP durum koduna göre isSuccess değerini belirleyen yardımcı metot.
    public static class ResponseBuilder<T> {
        public ResponseBuilder<T> statusCode(int statusCode) {
            this.statusCode = statusCode;
            this.isSuccess = (statusCode >= 200 && statusCode < 300);
            return this;
        }
    }
}