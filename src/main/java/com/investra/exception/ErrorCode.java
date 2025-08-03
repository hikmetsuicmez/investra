package com.investra.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Genel hatalar (1000-1999)
    INTERNAL_ERROR(1000, "Sunucu hatası"),
    INVALID_REQUEST(1001, "Geçersiz istek"),
    DATABASE_ERROR(1002, "Veritabanı hatası"),

    // Doğrulama hataları (2000-2999)
    VALIDATION_ERROR(2000, "Doğrulama hatası"),
    INVALID_CREDENTIALS(2001, "Geçersiz kullanıcı adı veya şifre"),

    // Kaynak bulunamadı hataları (3000-3999)
    RESOURCE_NOT_FOUND(3000, "Kaynak bulunamadı"),
    USER_NOT_FOUND(3001, "Kullanıcı bulunamadı"),
    STOCK_NOT_FOUND(3002, "Hisse senedi bulunamadı"),
    CLIENT_NOT_FOUND(3003, "Müşteri bulunamadı"),
    ACCOUNT_NOT_FOUND(3004, "Hesap bulunamadı"),

    // İzin/Yetki hataları (4000-4999)
    UNAUTHORIZED(4000, "Yetkisiz erişim"),
    FORBIDDEN(4001, "Bu işlem için yetkiniz bulunmamaktadır"),

    // İşlem hataları (5000-5999)
    OPERATION_FAILED(5000, "İşlem başarısız"),
    INSUFFICIENT_STOCK(5001, "Yetersiz hisse senedi miktarı"),
    INSUFFICIENT_FUNDS(5002, "Yetersiz bakiye"),
    INACTIVE_STOCK(5003, "Hisse senedi aktif değil"),
    CALCULATION_ERROR(5004, "Hesaplama hatası"),

    // Bildirim hataları (6000-6999)
    NOTIFICATION_ERROR(6000, "Bildirim gönderimi başarısız"),
    EMAIL_SENDING_ERROR(6001, "E-posta gönderimi başarısız");

    private final int code;
    private final String message;
}