package com.investra.entity;

import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
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

    // Hisse senedi kodu (örn. GARAN, THYAO)
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    // Emir referans numarası
    @Column(name = "order_reference", nullable = false)
    private String orderReference;

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

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    // Komisyon tutarı
    @Column(name = "commission", nullable = false, precision = 19, scale = 4)
    private BigDecimal commission;

    // BSMV tutarı
    @Column(name = "bsmv", nullable = false, precision = 19, scale = 4)
    private BigDecimal bsmv;

    // Net tutar (toplam tutardan komisyon ve vergi düşülmüş hali)
    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmount;

    // Valör tarihi bilgisi (T+2 veya T+3)
    @Column(name = "value_date", nullable = false)
    private String valueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;
}
