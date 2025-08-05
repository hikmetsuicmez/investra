package com.investra.enums;

public enum OrderStatus {
    PENDING("Bekleyen"),
    COMPLETED("Gerçekleşen"),
    PARTIALLY_COMPLETED("Kısmen Gerçekleşen"),
    CANCELLED("İptal Edilen"),
    SETTLED("Takas Tamamlandı"); // T+2 süreci tamamlandığında kullanılacak durum

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
