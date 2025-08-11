package com.investra.enums;

public enum TaxType {
    EXEMPT("muaf"), // Muaf
    NARROW_TAXPAYER("dar mükellef"),
    FULL_TAXPAYER("tam mükellef"); //

    private final String displayName;

    TaxType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
