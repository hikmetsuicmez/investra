package com.investra.constants;

public class ApiEndpoints {

    private ApiEndpoints() {
    }

    public static final class Auth {
        private Auth() {
        }

        public static final String BASE = "/api/v1/auth";
        public static final String LOGIN = "/login"; // Giriş yapma
        public static final String CHANGE_PASSWORD = "/change-password"; // Şifre değiştirme
        public static final String FORGOT_PASSWORD = "/forgot-password"; // Şifremi unuttum
        public static final String RESET_PASSWORD = "/reset-password";  // Şifre sıfırlama
    }

    public static final class User {
        private User() {
        }

        public static final String BASE = "/api/v1/users";
        public static final String CREATE = "/create-user";
        public static final String UPDATE = "/update-user/{employeeNumber}";
        public static final String DELETE = "/delete-user/{employeeNumber}";
        public static final String GET_ALL = "/get-all"; // Tüm kullanıcıları getir
        public static final String GET_BY_ID = "/{userId}"; // Kullanıcıyı ID ile getir
    }

    public static final class Stock {
        private Stock() {
        }

        public static final String BASE = "/api/v1/stocks";
        public static final String SELL = "/sell"; // Hisse senedi satışı
        public static final String BUY = "/buy"; // Hisse senedi alımı
        public static final String GET_ALL = "/all"; // Tüm hisse senetlerini getir
        public static final String GET_BY_CODE = "/{stockCode}"; // Hisse senedini koduna göre getir
        public static final String REFRESH = "/refresh"; // Hisse senedi verilerini güncelle
        public static final String SEARCH_CLIENT = "/search-client"; // Hisse senedi arama
        public static final String CLIENT_STOCK_HOLDINGS = "/client/{clientId}/stocks"; // Müşterinin Hisse senedi detayları
        public static final String AVAILABLE_STOCKS = "/available"; // Mevcut hisse senetleri
        public static final String PREVIEW_SELL_ORDER = "/preview"; // Satış önizleme
        public static final String PREVIEW_BUY_ORDER = "/preview"; // Alış önizleme
        public static final String EXECUTE_SELL_ORDER = "/execute"; // Satış işlemi
        public static final String EXECUTE_BUY_ORDER = "/execute"; // Alış işlemi
    }

    public static final class Client {
        private Client() {
        }

        public static final String BASE = "/api/v1/clients";
        public static final String CREATE = "/create-client";
        public static final String GET_CLIENT_INFO_BY_ID = "/client";
        public static final String DELETE = "/delete-client";
        public static final String PASSIVE_LIST = "/clients/passive-clients";
        public static final String ACTIVE_LIST = "/clients/active-clients";


    }

    public static final class Account {
        private Account() {
        }

        public static final String BASE = "/api/v1/accounts";
        public static final String DEPOSIT = "/deposit"; // Hesaba bakiye yükleme
        public static final String WITHDRAWAL = "/withdrawal"; // Hesaptan bakiye çıkışı
        public static final String CREATE = "/create";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String GET_BY_CLIENT = "/client/{clientId}";
        public static final String SEARCH_CLIENTS = "/search-clients";
        public static final String RECENT_CLIENTS = "/recent-clients";

    }

    public static final class TradeOrder {
        private TradeOrder() {
        }

        public static final String BASE = "/api/v1/trade-orders";
        public static final String GET_ALL = "/all"; // Tüm emirleri getir
        public static final String GET_PENDING = "/pending"; // Bekleyen emirleri getir
        public static final String GET_EXECUTED = "/executed"; // Gerçekleşen emirleri getir
        public static final String GET_COMPLETED = "/completed"; // Tamamlanmış emirleri getir
        public static final String GET_CANCELLED = "/cancelled"; // İptal edilen emirleri getir
        public static final String ORDER_CANCELLED = "/{orderId}/cancel";

    }

    public static final class Portfolio {
        private Portfolio() {}
        public static final String BASE = "/api/v1/portfolio";
        public static final String CREATE = ""; // Portföy oluşturma
        public static final String GET_ALL = ""; // Tüm portföyleri getir
        public static final String GET_BY_CLIENT_ID = "/{clientId}"; // Müşteri ID ile portföyü getir
        public static final String DELETE_BY_CLIENT_ID = "/{clientId}"; // Müşteri ID ile portföyü sil
    }

    public static final class EndOfDay {
        private EndOfDay() {
        }

        public static final String BASE = "/api/v1/end-of-day";
        public static final String STATUS = "/status"; // Gün sonu değerleme durumu
        public static final String FETCH_PRICES = "/fetch-prices"; // Kapanış fiyatlarını al

        public static final String CLIENT_VALUATIONS = "/client-valuations"; // Tüm müşteri değerlemelerini getir
        public static final String CLIENT_VALUATION = "/client-valuation/{clientId}"; // Belirli bir müşteri değerlemesini getir
        public static final String START_VALUATION = "/start-valuation"; // Gün sonu değerleme başlat
        public static final String STOCK_PRICES = "/stock-prices"; // Hisse fiyatlarını getir
        public static final String MANUALLY_UPDATE_PRICES = "/manually-update-prices"; // Kapanış fiyatlarını manuel olarak güncelle
    }
}
