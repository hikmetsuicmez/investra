package com.investra.dtos.request;

import com.investra.enums.ExecutionType;
import com.investra.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockSellOrderRequest {

    private Long clientId;
    private Long accountId;
    private Long stockId;
    private ExecutionType executionType; // LIMIT VE MARKET
    private Integer quantity;
    private BigDecimal price; // SADECE LIMIT EMIRLERINDE GEREKLI
    private Boolean previewConfirmed = false;

    public void validatePrice() {
        if (executionType == ExecutionType.LIMIT && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BadRequestException("Limit emirleri icin fiyat bilgisi zorunludur ve sifirdan buyuk olmalidir.");
        }
        if (executionType == ExecutionType.MARKET && price != null) {
            throw new BadRequestException("Market emirlerinde fiyat bilgisi gereksizdir ve kullanilamaz.");
        }
    }

}
