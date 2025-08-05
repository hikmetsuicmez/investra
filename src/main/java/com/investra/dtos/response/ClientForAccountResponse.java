package com.investra.dtos.response;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientForAccountResponse {
    private Long id;
    private String fullName;
    private String nationalityNumber;
    private String taxId;
    private String passportNo;
    private String blueCardNo;
    private String email;
    private String phone;
    private ClientStatus status;
    private ClientType clientType;
    private LocalDateTime createdAt;
    private int accountCount;  // müşterinin toplam hesap sayısı
}
