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
public class PortfolioPerformanceReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalClients;

    // Genel performans metrikleri
    private BigDecimal totalPortfolioValueChange;
    private BigDecimal totalPortfolioValueChangePercentage;
    private BigDecimal averagePortfolioValueChange;
    private BigDecimal averagePortfolioValueChangePercentage;

    // En iyi performans gösteren portföyler
    private List<BestPerformingPortfolio> bestPerformingPortfolios;

    // En kötü performans gösteren portföyler
    private List<WorstPerformingPortfolio> worstPerformingPortfolios;

    // Hisse senedi bazında performans
    private List<StockPerformance> stockPerformances;

    // Günlük performans değişimi
    private List<DailyPerformance> dailyPerformances;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BestPerformingPortfolio {
        private Long clientId;
        private String clientName;
        private String clientType;
        private BigDecimal startValue;
        private BigDecimal endValue;
        private BigDecimal changeValue;
        private BigDecimal changePercentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WorstPerformingPortfolio {
        private Long clientId;
        private String clientName;
        private String clientType;
        private BigDecimal startValue;
        private BigDecimal endValue;
        private BigDecimal changeValue;
        private BigDecimal changePercentage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StockPerformance {
        private String stockCode;
        private String stockName;
        private BigDecimal startPrice;
        private BigDecimal endPrice;
        private BigDecimal changePrice;
        private BigDecimal changePercentage;
        private Integer totalQuantity;
        private BigDecimal totalValue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DailyPerformance {
        private LocalDate date;
        private BigDecimal totalPortfolioValue;
        private BigDecimal changeValue;
        private BigDecimal changePercentage;
        private Integer activeClients;
    }
}
