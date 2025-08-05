package com.investra.dtos.request;

import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {

    private String nickname;

    @NotNull(message = "Hesap tipi boş olamaz")
    private AccountType accountType;

    @NotNull(message = "Para birimi boş olamaz")
    private Currency currency;

    @NotBlank(message = "Aracı kurum adı boş olamaz")
    private String brokerName;

    @NotBlank(message = "Aracı kurum kodu boş olamaz")
    private String brokerCode;

    @NotBlank(message = "Saklama kurumu adı boş olamaz")
    private String custodianName;

    @NotBlank(message = "Saklama kurumu kodu boş olamaz")
    private String custodianCode;

    @NotBlank(message = "IBAN boş olamaz")
    private String iban;

    @NotBlank(message = "Hesap numarası boş olamaz")
    private String accountNumberAtBroker;

    private BigDecimal initialBalance;

    @NotNull(message = "Müşteri ID boş olamaz")
    private Long clientId;
}
