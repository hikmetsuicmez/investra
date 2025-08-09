package com.investra.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientValuationResponse {
    private Long clientId;
    private String clientName;
    private BigDecimal totalPortfolioValue;
    private BigDecimal unrealizedProfitLoss;
    private BigDecimal dailyChangePercentage;
    private BigDecimal totalReturnPercentage;
    private LocalDate valuationDate;
    private List<StockPositionResponse> positions;
}
