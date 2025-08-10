package com.investra.dtos.request;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class CreateClientRequest {
    @NotNull(message = "Müşteri tipi zorunludur")
    protected ClientType clientType;

    @NotBlank(message = "Email alanı zorunludur")
    @Email(message = "Geçerli bir email adresi giriniz")
    protected String email;

    @NotBlank(message = "Telefon numarası zorunludur")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Telefon numarası yalnızca rakamlardan oluşmalı.")
    protected String phone;

    @NotBlank(message = "Adres alanı zorunludur")
    protected String address;
    protected String notes;
    protected ClientStatus status;
    protected Boolean isActive;
}