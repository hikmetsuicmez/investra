package com.investra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "stock_daily_prices", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"stock_id", "price_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDailyPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal openPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal closePrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal highPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal lowPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal volume;

    @Column(precision = 10, scale = 4)
    private BigDecimal changePercentage;

    @Column(nullable = false)
    private boolean officialClose;
}
