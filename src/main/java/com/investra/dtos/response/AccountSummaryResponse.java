package com.investra.dtos.response;

import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountSummaryResponse {
    private Long accountId;
    private String accountNumber;
    private Currency currency;
    private BigDecimal balance;
    private AccountType accountType;
}
