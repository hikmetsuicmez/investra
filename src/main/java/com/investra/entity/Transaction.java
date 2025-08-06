package com.investra.entity;

import com.investra.enums.TransactionStatus;
import com.investra.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_number", unique = true)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // İşlemi yapan kullanıcı

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "previous_balance", precision = 19, scale = 4)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", precision = 19, scale = 4)
    private BigDecimal newBalance;

    @Column(name = "description")
    private String description;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        // Transaction number otomatik oluştur
        if (transactionNumber == null || transactionNumber.trim().isEmpty()) {
            transactionNumber = generateTransactionNumber();
        }

        // İşlem tarihi belirtilmemişse bugünün tarihini kullan
        if (transactionDate == null) {
            transactionDate = LocalDate.now();
        }
    }

    // Benzersiz transaction number oluşturur
    private String generateTransactionNumber() {
        return "TRX" + System.currentTimeMillis();
    }
}
