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
public class StockResponse {
    private Long id;
    private String name;
    private String symbol;
    private BigDecimal currentPrice;
    private String stockGroup;
    private Boolean isActive;
    private String source;
    private String category;
}
