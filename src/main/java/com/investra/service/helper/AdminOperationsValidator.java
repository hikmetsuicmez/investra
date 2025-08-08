package com.investra.service.helper;

import java.util.function.Supplier;

public class AdminOperationsValidator {

    public static void duplicateResourceCheck(Supplier<Boolean> rule, String message) {
        if (rule.get()) {
            throw new IllegalArgumentException(message);
        }
    }
}