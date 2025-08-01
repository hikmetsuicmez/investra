package com.investra.dtos.request;

import com.investra.enums.ExecutionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockSellOrderRequest {

    @NotNull(message = "Müşteri ID zorunludur")
    private Long clientId;

    @NotNull(message = "Hesap ID zorunludur")
    private Long accountId;

    @NotNull(message = "Hisse senedi ID zorunludur")
    private Long stockId;

    @NotNull(message = "Emir tipi zorunludur")
    private ExecutionType executionType;

    @Positive(message = "Adet pozitif olmalıdır")
    private Integer quantity;

    // Price alanı sadece LIMIT emirlerde kullanılır
    private BigDecimal price;

    private Boolean previewConfirmed = false;

    // Price alanının validasyonu için custom validasyon
    public void validatePrice() {
        if (executionType == ExecutionType.LIMIT && (price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Limit emirlerde fiyat belirtilmelidir ve sıfırdan büyük olmalıdır");
        }
        if (executionType == ExecutionType.MARKET && price != null) {
            throw new RuntimeException("Piyasa emirlerinde fiyat belirtilemez");
        }
    }
}
