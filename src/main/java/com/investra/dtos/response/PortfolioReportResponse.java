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
public class PortfolioReportResponse {

    // Müşteri Bilgileri
    private String customerName;
    private String customerNumber;
    private String customerType;
    private String accountNumber;
    private LocalDate reportDate;

    // Portföy Değerleri
    private BigDecimal portfolioCurrentValue;
    private BigDecimal totalPositionValue;
    private BigDecimal tlBalance;

    // Hisse Senedi Detayları
    private List<StockPositionDetail> stockPositions;

    // Toplam Değerler
    private BigDecimal totalNominalValue;
    private BigDecimal totalPotentialProfitLoss;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StockPositionDetail {
        private String stockCode;
        private String stockName;
        private String category;

        // T+0, T+1, T+2 pozisyonları (BUY orders)
        private Integer t0Quantity;
        private Integer t1Quantity;
        private Integer t2Quantity;

        // T+0, T+1, T+2 pozisyonları (SELL orders) - ayrı kolonlar
        private Integer t0SellQuantity;
        private Integer t1SellQuantity;
        private Integer t2SellQuantity;

        // Fiyat bilgileri
        private BigDecimal buyPrice;
        private BigDecimal closingPrice;

        // Hesaplanan değerler
        private BigDecimal nominalValue;
        private BigDecimal potentialProfitLoss;
        private BigDecimal profitLossRatio;

        // Elde bulunan toplam adet (net pozisyon)
        private Integer totalQuantity;

        // Toplam SELL miktarı
        private Integer totalSellQuantity;
    }
}
