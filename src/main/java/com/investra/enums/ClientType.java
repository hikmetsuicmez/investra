package com.investra.enums;

public enum ClientType {
    INDIVIDUAL("Bireysel"),
    CORPORATE("Kurumsal");

    private final String displayName;

    ClientType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}

