package com.investra.dtos.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private boolean firstLogin;
}
