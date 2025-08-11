package com.investra.dtos.response;

import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import com.investra.enums.TaxType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientSearchResponse {
    private Long id;
    private String fullName;
    private String nationalityNumber;
    private String taxId;
    private String email;
    private String phoneNumber;
    private TaxType taxType;
    private ClientStatus clientStatus;
    private ClientType clientType; // BIREYSEL , KURUMSAL
    private LocalDateTime createdAt;
    private Boolean isActive;
    private String clientNumber;
    private List<AccountSummaryResponse> accounts;
}
