package com.investra.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioSummaryReportResponse {

    private LocalDate reportDate;
    private Integer totalClients;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCashBalance;
    private BigDecimal totalStockValue;

    // Müşteri tipine göre dağılım
    private List<ClientTypeSummary> clientTypeDistribution;

    // En büyük portföyler
    private List<TopPortfolio> topPortfolios;

    // En çok işlem yapılan hisseler
    private List<TopStock> topStocks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ClientTypeSummary {
        private String clientType;
        private Integer clientCount;
        private BigDecimal totalValue;
        private BigDecimal averageValue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopPortfolio {
        private Long clientId;
        private String clientName;
        private String clientType;
        private BigDecimal portfolioValue;
        private BigDecimal cashBalance;
        private BigDecimal stockValue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopStock {
        private String stockCode;
        private String stockName;
        private Integer totalQuantity;
        private BigDecimal totalValue;
        private Integer clientCount;
    }
}
