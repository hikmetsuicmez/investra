package com.investra.dtos.response;

import com.investra.enums.ExecutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBuyOrderPreviewResponse {
    private String accountNumber;
    private String operation; // "Alış"
    private String stockName;
    private String stockSymbol;
    private BigDecimal price;
    private Integer quantity;
    private LocalDate tradeDate;
    private String valueDate; // T+2 gibi
    private BigDecimal totalAmount;
    private String stockGroup;
    private BigDecimal commission;
    private BigDecimal bsmv;
    private BigDecimal totalTaxAndCommission;
    private BigDecimal netAmount; // Toplam tutar + komisyon + vergi
    private ExecutionType executionType;
    private String previewId; // Önizleme ID'si (execute ederken kontrol için)

    // İlişkili nesneler hakkında bilgi
//    private ClientInfoResponse clientInfo;
//    private AccountInfoResponse accountInfo;
//    private StockInfoResponse stockInfo;
}
