package com.investra.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.investra.enums.StockGroup;
import com.investra.enums.StockSource;
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
@Table(name = "stocks")
@Builder
@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true)
    private String code; // Infina API'sinde HISSE_KODU olarak gelen alan

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "sector")
    private String sector; // Infina API'sinde SEKTOR_ADI olarak gelen alan

    @Column(name = "exchange_code")
    private String exchangeCode; // Infina API'sinde BORSA_KODU olarak gelen alan

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_group")
    private StockGroup group;

    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price; // Infina API'sinde FIYAT olarak gelen alan

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private StockSource source;

    @Column(name = "created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
