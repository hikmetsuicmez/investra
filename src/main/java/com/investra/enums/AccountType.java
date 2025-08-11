package com.investra.enums;

public enum AccountType {
    SETTLEMENT("Takas Hesabı"),
    DEPOSIT("Mevduat Hesabı"),
    BLOKED("Bloke Hesabı");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
