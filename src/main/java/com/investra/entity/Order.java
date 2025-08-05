package com.investra.entity;

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

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String stockSymbol;

    @Column(nullable = false)
    private String stockName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // T+2 takas durumu
    @Enumerated(EnumType.STRING)
    private SettlementStatus settlementStatus;

    // İlgili TradeOrder ID'si
    private Long tradeOrderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    // T+2 işlemleri için tamamlanma zamanı
    private LocalDateTime settlementDate;

    // Fonların bloke edilip edilmediğini belirtir
    private boolean fundsReserved;

    // Planlanan işlem için sona erme tarihi (örn. bekleyen emirler için)
    private LocalDateTime expirationDate;

    // Alım/satım gerçekleştiğinde toplam tutar
    private BigDecimal totalAmount;

    // İşlem referans numarası
    private String transactionReference;

    // T+2 süreci tamamlandıktan sonra portfolyo güncellemesi yapıldı mı?
    private boolean portfolioUpdated;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = OrderStatus.PENDING;
        }

        // Eğer gerçekleşen emirse ve settlement status null ise, takas bekleniyor olarak ayarla
        if (status == OrderStatus.COMPLETED && settlementStatus == null) {
            settlementStatus = SettlementStatus.PENDING;
        }

        // Rastgele transactionReference oluştur
        if (transactionReference == null) {
            transactionReference = generateTransactionReference();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateTransactionReference() {
        return "TRX" + System.currentTimeMillis() + (int) (Math.random() * 10000);
    }
}
