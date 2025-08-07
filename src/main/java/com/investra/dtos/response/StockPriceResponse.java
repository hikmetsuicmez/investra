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
public class StockPriceResponse {
    private String stockCode;
    private String companyName;
    private String sector;
    private BigDecimal closePrice;
    private BigDecimal changePercentage;
    private BigDecimal volume;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
}
