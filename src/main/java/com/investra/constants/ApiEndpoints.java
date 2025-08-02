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

    private static final class Stock {
        private Stock() {}

        // Stok işlemleri ile ilgili endpoint'ler burada tanımlanabilir
    }

}
