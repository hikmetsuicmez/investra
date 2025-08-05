package com.investra.dtos;

import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDto {

    private String orderNumber; // İsteğe bağlı, otomatik oluşturulabilir

    @NotNull(message = "Müşteri ID boş olamaz")
    private Long clientId;

    @NotNull(message = "Hesap ID boş olamaz")
    private Long accountId;

    @NotNull(message = "Hisse senedi ID boş olamaz")
    private Long stockId;

    // Hisse sembolü ve adı, hisse ID üzerinden çekilebilir (isteğe bağlı)
    private String stockSymbol;
    private String stockName;

    @NotNull(message = "Miktar boş olamaz")
    @Positive(message = "Miktar pozitif olmalıdır")
    private Integer quantity;

    // Fiyat, hisse ID'den otomatik alınabilir (isteğe bağlı)
    private BigDecimal price;

    // Hesaplanan değerler, backend tarafında otomatik hesaplanır (isteğe bağlı)
    private BigDecimal totalAmount;
    private BigDecimal netAmount;

    @NotNull(message = "Emir tipi boş olamaz")
    private OrderType orderType;

    @NotNull(message = "Gerçekleştirme tipi boş olamaz")
    private ExecutionType executionType;

    // Durum varsayılan olarak PENDING olarak ayarlanabilir (isteğe bağlı)
    private OrderStatus status;

    // İlgili TradeOrder kaydının ID'si (isteğe bağlı)
    private Long tradeOrderId;
}
