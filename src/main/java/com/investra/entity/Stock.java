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
import java.time.format.DateTimeFormatter;

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
    private LocalDateTime createdAt;

    @Transient
    private String createdAtString;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private String updatedAtString;

    public String getCreatedAt() {
        if (createdAt != null) {
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return null;
    }

    public String getUpdatedAt() {
        if (updatedAt != null) {
            return updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return null;
    }

    public LocalDateTime getCreatedAtDate() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAtDate() {
        return updatedAt;
    }

    public void setCreatedAtDate(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAtDate(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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
