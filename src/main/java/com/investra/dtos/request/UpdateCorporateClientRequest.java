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
public class UpdateCorporateClientRequest extends UpdateClientRequest {

    private String companyName;

    private String taxNumber;

    private String registrationNumber;

    private String companyType;

    private String sector;

    @Positive(message = "Ciro pozitif bir değer olmalıdır")
    private BigDecimal monthlyRevenue;
}
