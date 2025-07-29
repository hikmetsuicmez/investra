package com.investra.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut şifre boş olamaz")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 8 karakter olmalıdır")
    @Size(max = 20, message = "Şifre en fazla 20 karakter olmalıdır")
    private String newPassword;

    @NotBlank(message = "Şifre onayı boş olamaz")
    private String confirmPassword;
}
