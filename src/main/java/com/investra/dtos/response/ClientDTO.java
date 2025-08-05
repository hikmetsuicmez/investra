package com.investra.dtos.response;


import com.investra.enums.ClientType;
import com.investra.enums.ClientStatus;
import com.investra.enums.Gender;
import com.investra.enums.EstimatedTransactionVolume;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO {

    private Long id;

    private ClientType clientType;

    private String email;

    private String phone;

    private String address;

    private String notes;

    private ClientStatus status;

    private Boolean isActive;

    private LocalDateTime createdAt;

    // Bireysel müşteri alanları
    private String fullName;

    private Boolean nationalityType;

    private String taxId;

    private String passportNo;

    private String blueCardNo;

    private String nationalityNumber;

    private LocalDate birthDate;

    private String profession;

    private Gender gender;

    private String educationStatus;

    private BigDecimal monthlyIncome;

    private EstimatedTransactionVolume estimatedTransactionVolume;

    // Kurumsal müşteri alanları
    private String companyName;

    private String taxNumber;

    private String registrationNumber;

    private String companyType;

    private String sector;

    private BigDecimal monthlyRevenue;

}

