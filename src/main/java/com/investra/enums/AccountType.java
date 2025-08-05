package com.investra.enums;

public enum AccountType {
    SETTLEMENT("Takas Hesabı"),
    INVESTMENT("Yatırım Hesabı"),
    CURRENT("Cari Hesap"),
    SAVINGS("Tasarruf Hesabı"),
    DEPOSIT("Mevduat Hesabı");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
