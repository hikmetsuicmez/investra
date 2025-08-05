package com.investra.enums;

public enum OrderStatus {
    PENDING("Bekleyen"),
    EXECUTED("Gerçekleşen"),
    SETTLED("Takas Tamamlandı"),
    CANCELLED("İptal Edilen"),
    REJECTED("Reddedildi");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}