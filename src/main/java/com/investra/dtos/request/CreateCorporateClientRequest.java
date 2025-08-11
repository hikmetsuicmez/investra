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

    @NotBlank(message = "Şirket isim alanı zorunludur")
    private String companyName;

    @NotBlank(message = "Vergi numarası zorunludur")
    private String taxNumber;

    @NotBlank(message = "Sicil numarası zorunludur")
    private String registrationNumber;

    @NotBlank(message = "Şirket türü zorunludur")
    private String companyType;

    @NotBlank(message = "Faaliyet alanı zorunludur")
    private String sector;

    @Positive(message = "Ciro pozitif bir değer olmalıdır")
    private BigDecimal monthlyRevenue;
}
