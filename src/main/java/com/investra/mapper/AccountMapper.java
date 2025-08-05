package com.investra.mapper;

import com.investra.dtos.response.AccountResponse;
import com.investra.entity.Account;

public class AccountMapper {

    public static AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .id(account.getId())
                .nickname(account.getNickname())
                .accountNumber(account.getAccountNumber())
                .iban(account.getIban())
                .accountNumberAtBroker(account.getAccountNumberAtBroker())
                .brokerName(account.getBrokerName())
                .brokerCode(account.getBrokerCode())
                .custodianName(account.getCustodianName())
                .custodianCode(account.getCustodianCode())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .accountType(account.getAccountType())
                .isPrimarySettlement(account.isPrimarySettlement())
                .createdAt(account.getCreatedAt())
                .clientName(account.getClient() != null ? account.getClient().getFullName() : null)
                .clientId(account.getClient() != null ? account.getClient().getId() : null)
                .build();
    }
}
