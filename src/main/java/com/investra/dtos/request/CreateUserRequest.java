package com.investra.dtos.request;

import com.investra.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {
    @NotBlank(message = "İsim alanı zorunludur")
    private String firstName;

    @NotBlank(message = "Soyisim alanı zorunludur")
    private String lastName;

    @NotBlank(message = "TC kimlik no zorunludur")
    @Pattern(regexp = "\\d{11}", message = "TC kimlik no 11 haneli sayı olmalıdır")
    private String nationalityNumber;

    @NotBlank(message = "Telefon numarası zorunludur")
    private String phoneNumber;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçersiz e-posta adresi")
    private String email;

    @NotNull(message = "Personel yetki seçimi zorunludur")
    private Role role;

    private boolean firstLogin = true;


}
