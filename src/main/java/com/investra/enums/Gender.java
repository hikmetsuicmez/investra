package com.investra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Gender {
    Male,
    Female,
    Other;

    @JsonCreator
    public static Gender fromString(String value) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid gender: " + value));
    }
}
