package com.investra.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email adresi boş olamaz")
    @Email(message = "Geçerli bir email formatı giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;
}
