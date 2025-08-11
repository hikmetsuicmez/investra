package com.investra.dtos.request;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;

import com.investra.enums.TaxType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @NotBlank(message = "Email adresi zorunludur")
    @Email(message = "Geçerli bir email adresi giriniz (örn: ornek@email.com)")
    protected String email;

    @NotBlank(message = "Telefon numarası zorunludur")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Telefon numarası 10-15 haneli rakam olmalıdır (örn: 05551234567)")
    protected String phone;

    @NotNull(message = "Vergi tipi zorunludur")
    protected TaxType taxType;

    protected String address;
    protected String notes;
    protected ClientStatus status;
    protected Boolean isActive;
}