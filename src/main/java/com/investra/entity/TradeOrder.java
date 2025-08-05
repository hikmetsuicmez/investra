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

    // Rastgele duruma atama için yardımcı metot
    @Transient
    public void assignRandomStatus() {
        int randomValue = (int) (Math.random() * 100);

        if (randomValue < 60) {
            // %60 ihtimalle bekleyen emir
            this.status = OrderStatus.PENDING;
        } else if (randomValue < 90) {
            // %30 ihtimalle gerçekleşen emir
            this.status = OrderStatus.EXECUTED;
            // T+2 sistemine göre 2 gün sonra takas tamamlanacak
            this.settlementStatus = SettlementStatus.PENDING;
            this.settlementDate = LocalDateTime.now().plusSeconds(15); // Test için 15 saniye
            this.fundsReserved = true;
        } else {
            // %10 ihtimalle iptal edilen emir
            this.status = OrderStatus.CANCELLED;
        }
    }
}
