package com.investra.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPositionResponse {
    private String stockCode;
    private String stockName;
    private Integer quantity;
    private BigDecimal costPrice;
    private BigDecimal currentPrice;
    private BigDecimal positionValue;
    private BigDecimal unrealizedProfitLoss;
    private BigDecimal changePercentage;
}
