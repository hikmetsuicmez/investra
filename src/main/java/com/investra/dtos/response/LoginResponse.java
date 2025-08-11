package com.investra.dtos.response;

import com.investra.enums.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private boolean firstLogin;
    private Role role;
}
