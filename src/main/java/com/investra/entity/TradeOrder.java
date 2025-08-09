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

    // T+2 sistemi için gerekli alanlar
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status")
    private SettlementStatus settlementStatus;

    @Column(name = "funds_reserved")
    private boolean fundsReserved;

    // Simülasyon tabanlı T+2 sistemi
    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "settlement_days_remaining")
    private Integer settlementDaysRemaining;

    // Rastgele duruma atama için yardımcı metot
    @Transient
    public void assignRandomStatus() {
        assignRandomStatus(LocalDate.now());
    }

    // Belirli bir tarihle rastgele durum atama
    @Transient
    public void assignRandomStatus(LocalDate currentDate) {
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
            // Gün bazlı T+2 sistemi için ayarlar (haftasonu yok, her zaman hafta içi)
            this.tradeDate = currentDate;
            this.settlementStatus = SettlementStatus.PENDING;
            this.settlementDaysRemaining = 2;
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

    // Simülasyon tarihi ile settlement kontrolü
    @Transient
    public boolean isReadyForSettlement(LocalDate currentSimulationDate) {
        if (this.status != OrderStatus.EXECUTED || this.settlementStatus == SettlementStatus.COMPLETED) {
            return false;
        }

        if (this.tradeDate == null)
            return false;

        // T+2 = trade + 2 gün, yani 3. günde settlement
        LocalDate expectedSettlementDate = this.tradeDate.plusDays(2);
        return !currentSimulationDate.isBefore(expectedSettlementDate);
    }

    // Settlement günlerini güncelleme (status'a göre doğru hesaplama)
    @Transient
    public void updateSettlementDaysRemaining(LocalDate currentSimulationDate) {
        if (this.tradeDate != null && this.status == OrderStatus.EXECUTED) {
            // Status'a göre doğru kalan gün sayısını hesapla
            switch (this.settlementStatus) {
                case PENDING:
                    this.settlementDaysRemaining = 2;
                    break;
                case T1:
                    this.settlementDaysRemaining = 1;
                    break;
                case T2:
                    this.settlementDaysRemaining = 0;
                    break;
                case COMPLETED:
                    this.settlementDaysRemaining = 0;
                    break;
                default:
                    // Tarih bazlı hesaplama (fallback)
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(this.tradeDate,
                            currentSimulationDate);
                    this.settlementDaysRemaining = Math.max(0, 2 - (int) daysBetween);
            }
        }
    }
}
