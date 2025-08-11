package com.investra.dtos.response;

import com.investra.enums.ExecutionType;
import com.investra.enums.OrderType;
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
    private String stockName;
    private String stockSymbol;
    private BigDecimal price; // (HISSE SENEDI ADET FİYATI)
    private Integer quantity; // (HISSE SENEDI ADEDİ)
    private BigDecimal totalAmount; // (HISSE SENEDI ADEDİ * ADET FİYATI)
    private BigDecimal commission; // (TOPLAM TUTARIN %0.2'İ BİREYSEL ICIN, TOPLAM TUTARIN %0.1'İ KURUMSAL ICIN)
    private BigDecimal bsmv; // (Komisyonun %5'i BSMV)
    private BigDecimal totalTaxAndCommission; // (KOMİSYON + BSMV)
    private BigDecimal netAmount; // (TOPLAM TUTAR - KOMİSYON - BSMV)
    private ExecutionType executionType; // "LIMIT" veya "MARKET"
    private StockGroup stockGroup; // TECHNOLOGY, FINANCE, HEALTHCARE, ENERGY gibi gruplar
    private OrderType orderType; // "BUY" veya "SELL"
    private LocalDate tradeDate; // Emir tarihini belirtir, genellikle bugünün tarihi
    private String valueDate; // Vade tarihi, genellikle emir tarihinden 2 iş günü sonrasını ifade eder

    // Önizleme ID'si, satış işlemi için gereklidir
    private String previewId;

    // Önizlemenin geçerlilik süresi (dakika)
    private Integer expiryMinutes = 10;

    private String category;
}
