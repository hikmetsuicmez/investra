package com.investra.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 8 karakter olmalıdır")
    private String password;

    @NotBlank(message = "Şifre tekrarı boş olamaz")
    private String confirmPassword;
}
