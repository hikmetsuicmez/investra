package com.investra.dtos.response;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateClientResponse {
    private Long id;
    private String clientNumber;
    private ClientType clientType;
    private String email;
    private String phone;
    private String address;
    private String notes;
    private ClientStatus status;
    private Boolean isActive;
    private LocalDateTime updatedAt;

    // Bireysel müşteri alanları
    private String fullName;
    private String nationalityNumber;
    private String passportNo;
    private String blueCardNo;
    private String taxId;

    // Kurumsal müşteri alanları
    private String companyName;
    private String taxNumber;
    private String registrationNumber;
    private String companyType;
    private String sector;
}
