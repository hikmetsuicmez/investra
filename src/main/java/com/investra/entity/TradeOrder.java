package com.investra.entity;

import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "trade_orders")
@Builder
@Entity
public class TradeOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false)
    private ExecutionType executionType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    private String orderNumber;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User user;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    // T+2 sistemi için yeni alanlar
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status")
    private SettlementStatus settlementStatus;

    @Column(name = "settlement_date")
    private LocalDateTime settlementDate;

    @Column(name = "funds_reserved")
    private boolean fundsReserved;

    @Column(name = "portfolio_updated")
    private boolean portfolioUpdated;

    // Gün bazlı T+2 sistemi için yeni alanlar
    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "settlement_days_remaining")
    private Integer settlementDaysRemaining;

    @Column(name = "expected_settlement_date")
    private LocalDate expectedSettlementDate;

    // Rastgele duruma atama için yardımcı metot
    @Transient
    public void assignRandomStatus() {
        int randomValue = (int) (Math.random() * 100);

        if (this.executionType == ExecutionType.LIMIT) {
            this.status = OrderStatus.PENDING;
            return;
        }
        if (randomValue < 60) {
            // %60 ihtimalle bekleyen emir
            this.status = OrderStatus.PENDING;
        } else if (randomValue < 90) {
            // %30 ihtimalle gerçekleşen emir
            this.status = OrderStatus.EXECUTED;
            // Gün bazlı T+2 sistemi için ayarlar
            this.tradeDate = LocalDate.now();
            this.settlementStatus = SettlementStatus.PENDING;
            this.settlementDaysRemaining = 2;
            this.expectedSettlementDate = LocalDate.now().plusDays(2);
            this.fundsReserved = true;
        } else {
            // %10 ihtimalle iptal edilen emir
            this.status = OrderStatus.CANCELLED;
            // İptal edilen emirler için settlement status'u da CANCELLED yap
            this.settlementStatus = SettlementStatus.CANCELLED;
            this.settlementDaysRemaining = 0;
            this.fundsReserved = false;
        }
    }
}
