package com.investra.dtos;

import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String stockSymbol;
    private String stockName;
    private Integer quantity;
    private BigDecimal price;
    private OrderType orderType;
    private OrderStatus status;
    private SettlementStatus settlementStatus;
    private Long tradeOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime settlementDate;
    private boolean fundsReserved;
    private LocalDateTime expirationDate;
    private BigDecimal totalAmount;
    private String transactionReference;
    private boolean portfolioUpdated;
}
