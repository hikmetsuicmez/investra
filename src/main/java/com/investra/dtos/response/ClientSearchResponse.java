package com.investra.dtos.response;

import com.investra.enums.ClientStatus;
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
    private String tckn;
    private String vergiNo;
    private String blueCardNo;
    private String email;
    private String phone;
    private ClientStatus status;
    private String clientType; // "BIREYSEL" veya "KURUMSAL"
    private LocalDateTime createdAt;
    private List<AccountSummaryResponse> accounts;
}
