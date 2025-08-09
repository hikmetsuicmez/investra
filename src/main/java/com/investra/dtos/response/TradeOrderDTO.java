package com.investra.dtos.response;

import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeOrderDTO {
    private Long id;
    private Long clientId;
    private String clientFullName;
    private String stockCode;
    private OrderType orderType;
    private Integer quantity;
    private BigDecimal price;
    private String status;
    private LocalDateTime submittedAt;
    private LocalDateTime settledAt;
    private SettlementStatus settlementStatus;
    private java.time.LocalDate tradeDate;
    private Integer settlementDaysRemaining;

}
