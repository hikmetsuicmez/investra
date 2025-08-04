package com.investra.dtos.response;

import com.investra.enums.StockGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientStockHoldingResponse {

    private Long stockId;
    private String stockSymbol;
    private String stockName;
    private StockGroup stockGroup;
    private Integer availableQuantity;
    private BigDecimal currentPrice;
    private BigDecimal avgPrice;
}
