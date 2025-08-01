package com.investra.dtos.response;

import com.investra.enums.ExecutionType;
import com.investra.enums.StockGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockSellOrderPreviewResponse {
    private String accountNumber;
    private String operation;
    private String stockName;
    private String stockSymbol;
    private BigDecimal price;
    private Integer quantity;
    private LocalDate tradeDate;
    private String valueDate;
    private BigDecimal totalAmount;
    private StockGroup stockGroup;
    private BigDecimal commission;
    private BigDecimal bsmv;
    private BigDecimal totalTaxAndCommission;
    private BigDecimal netAmount;
    private ExecutionType executionType;
    private String valueType;

    @Data
    @Builder
    public static class ClientInfo {
        private String fullName;
        private String clientType;
        private String tckn;
    }

    @Data
    @Builder
    public static class AccountInfo {
        private String accountNumber;
        private BigDecimal currentBalance;
        private String currency;
        private boolean isPrimaryTakas;
    }

    @Data
    @Builder
    public static class StockInfo {
        private String symbol;
        private String name;
        private BigDecimal currentPrice;
        private Integer availableQuantity;
        private String group;
    }

    private ClientInfo clientInfo;
    private AccountInfo accountInfo;
    private StockInfo stockInfo;
}
