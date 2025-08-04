package com.investra.dtos.response;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import com.investra.enums.EstimatedTransactionVolume;
import com.investra.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateClientResponse {
    //ortak alan
    private ClientType clientType;
    private String email;
    private String phone;
    private String address;
    private String notes;
    private ClientStatus status;
    private Boolean isActive;

    //bireysel müşteri
    private String fullName;
    private Boolean nationalityType; // true: TC, false: Yabancı
    private String taxId;
    private String passportNo;
    private String blueCardNo;
    private String nationalityNumber;
    private LocalDate birthDate;
    private String prosfession;
    private Gender gender;
    private String educationStatus;
    private BigDecimal monthlyIncome;
    private EstimatedTransactionVolume estimatedTransactionVolume;

    //kurumsal müşteri
    private String companyName;
    private String taxNumber;
    private String registrationNumber;
    private String companyType;
    private String sector;
    private BigDecimal monthlyRevenue;
}
