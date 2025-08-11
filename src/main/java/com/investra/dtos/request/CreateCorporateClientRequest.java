package com.investra.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class CreateCorporateClientRequest extends CreateClientRequest {

    @NotBlank(message = "Şirket adı zorunludur")
    private String companyName;

    @NotBlank(message = "Vergi numarası zorunludur (10 haneli)")
    private String taxNumber;

    private String registrationNumber;

    @NotBlank(message = "Şirket türü zorunludur (Anonim, Limited, vb.)")
    private String companyType;

    @NotBlank(message = "Faaliyet alanı zorunludur (Finans, Teknoloji, vb.)")
    private String sector;

    @Positive(message = "Ciro pozitif bir değer olmalıdır")
    private BigDecimal monthlyRevenue;
}
