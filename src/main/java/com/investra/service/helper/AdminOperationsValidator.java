package com.investra.service.helper;

import com.investra.exception.ErrorCode;

import java.util.function.Supplier;

public class AdminOperationsValidator {

    public static void duplicateResourceCheck(Supplier<Boolean> rule, String message, ErrorCode operationFailed) {
        if (rule.get()) {
            throw new IllegalArgumentException(message);
        }
    }
}