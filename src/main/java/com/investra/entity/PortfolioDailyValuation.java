package com.investra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_daily_valuations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDailyValuation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate valuationDate;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalPortfolioValue;

    @Column(precision = 19, scale = 4)
    private BigDecimal unrealizedProfitLoss;

    @Column(precision = 10, scale = 4)
    private BigDecimal dailyChangePercentage;

    @Column(precision = 10, scale = 4)
    private BigDecimal totalReturnPercentage;

    @Column(precision = 19, scale = 4)
    private BigDecimal initialInvestment;

    @Column(precision = 19, scale = 4)
    private BigDecimal previousDayValue;

    @Column(nullable = false)
    private boolean locked;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
}
