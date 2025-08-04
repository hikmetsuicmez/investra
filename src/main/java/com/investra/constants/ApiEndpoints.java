package com.investra.constants;

public class ApiEndpoints {

    private ApiEndpoints() {}

    public static final class Auth {
        private Auth() {}

        public static final String BASE = "/api/v1/auth";
        public static final String LOGIN = "/login"; // Giriş yapma
        public static final String CHANGE_PASSWORD = "/change-password"; // Şifre değiştirme
        public static final String FORGOT_PASSWORD = "/forgot-password"; // Şifremi unuttum
        public static final String RESET_PASSWORD = "/reset-password";  // Şifre sıfırlama
    }

     public static final class User {
        private User() {}
         public static final String BASE = "/api/v1/users";
         public static final String CREATE = "/create-user";
         public static final String UPDATE = "/update-user/{employeeNumber}";
         public static final String DELETE = "/delete-user/{employeeNumber}";
    }

    public static final class Stock {
        private Stock() {}

        public static final String BASE = "/api/v1/stocks";
        public static final String SELL = "/sell"; // Hisse senedi satışı
        public static final String BUY = "/buy"; // Hisse senedi alımı
        public static final String SEARCH_CLIENT = "/search-client"; // Hisse senedi arama
        public static final String CLIENT_STOCK_HOLDINGS = "/client/{clientId}/stocks"; // Müşterinin Hisse senedi detayları
        public static final String AVAILABLE_STOCKS = "/available"; // Mevcut hisse senetleri
        public static final String PREVIEW_SELL_ORDER = "/preview"; // Satış önizleme
        public static final String PREVIEW_BUY_ORDER = "/preview"; // Alış önizleme
        public static final String EXECUTE_SELL_ORDER = "/execute"; // Satış işlemi
        public static final String EXECUTE_BUY_ORDER = "/execute"; // Alış işlemi
    }

    public static final class Client {
        private Client() {}
        public static final String BASE = "/api/v1/clients";
        public static final String CREATE = "/create-client";
        public static final String GET_CLIENT_INFO_BY_ID = "/client/{nationalityNumber}";
    }

    public static final class Account {
        private Account() {}
        public static final String BASE = "/api/v1/accounts";
        public static final String CREATE = "/create";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String GET_BY_CLIENT = "/client/{clientId}";
        public static final String SEARCH_CLIENTS = "/search-clients";
        public static final String RECENT_CLIENTS = "/recent-clients";
    }
}
