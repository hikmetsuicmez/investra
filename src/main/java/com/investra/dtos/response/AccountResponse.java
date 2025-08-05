package com.investra.dtos.response;

import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String nickname;
    private String accountNumber;
    private String iban;
    private String accountNumberAtBroker;
    private String brokerName;
    private String brokerCode;
    private String custodianName;
    private String custodianCode;
    private Currency currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private AccountType accountType;
    private boolean isPrimarySettlement;
    private LocalDateTime createdAt;
    private String clientName;
    private Long clientId;
}
