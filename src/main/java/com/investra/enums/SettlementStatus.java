package com.investra.enums;

public enum SettlementStatus {
    PENDING("Takas Bekliyor"),
    PENDING_WEEKEND("Haftasonu Bekliyor"),
    T1("T+1"),
    T2("T+2"),
    READY_FOR_SETTLEMENT("Settlement İçin Hazır"),
    SETTLED("Settled"),
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
