package com.investra.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstimatedTransactionVolume {
    RANGE_0_500K("0-500.000"),
    RANGE_500K_1_5M("500.000-1.500.000"),
    RANGE_1_5M_3M("1.500.000-3.000.000"),
    RANGE_3M_PLUS("3.000.000 ve üzeri");

    private final String label;

    EstimatedTransactionVolume(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static EstimatedTransactionVolume fromLabel(String label) {
        for (EstimatedTransactionVolume value : values()) {
            if (value.label.equalsIgnoreCase(label.trim())) {
                return value;
            }
        }
        throw new IllegalArgumentException("Geçersiz işlem hacmi: " + label);
    }
}
