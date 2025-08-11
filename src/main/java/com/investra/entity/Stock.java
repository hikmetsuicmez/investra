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
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "sector")
    private String sector;

    @Column(name = "exchange_code")
    private String exchangeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_group")
    private StockGroup group;

    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "isin")
    private String isin;

    @Column(name = "market")
    private String market;

    @Column(name = "sub_market")
    private String subMarket;

    @Column(name = "currency")
    private String currency;

    @Column(name = "category")
    private String category;

    @Column(name = "security_desc")
    private String securityDesc;

    @Column(name = "issuer_name")
    private String issuerName;

    @Column(name = "last_price_update")
    private LocalDateTime lastPriceUpdate;

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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (isActive == null) {
            isActive = true;
        }

        if (category == null) {
            category = "A";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
