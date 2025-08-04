package com.investra.entity;


import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id",referencedColumnName = "id")
    private Client client;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "iban")
    private String iban;

    @Column(name = "account_number_at_broker")
    private String accountNumberAtBroker;

    @Column(name = "broker_name")
    private String brokerName;

    @Column(name = "broker_code")
    private String brokerCode;

    @Column(name = "custodian_name")
    private String custodianName;

    @Column(name = "custodian_code")
    private String custodianCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "available_balance", precision = 19, scale = 4, columnDefinition = "DECIMAL(19,4) DEFAULT 0.0000")
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    //yatırım hesabı mı yoksa takas hesabı mı
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    //farklı currencylerde varsa hangisi primary belirtmek için
    @Column(name = "is_primary_settlement")
    private boolean isPrimarySettlement;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Account number otomatik oluştur
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            accountNumber = generateAccountNumber();
        }

        if (availableBalance == null) {
            availableBalance = balance;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Benzersiz account number oluşturur
    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("ACC%d%03d", timestamp, random);
    }
}
