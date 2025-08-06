package com.investra.enums;

public enum SettlementStatus {
    PENDING("Takas Bekliyor"),
    COMPLETED("Takas Tamamlandı"),
    CANCELLED("Takas İptal Edildi");

    private final String displayName;

    SettlementStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
