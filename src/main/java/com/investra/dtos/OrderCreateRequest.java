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
public class OrderCreateRequest {

    private String orderNumber;

    @NotNull(message = "Müşteri ID boş olamaz")
    private Long clientId;

    @NotNull(message = "Hesap ID boş olamaz")
    private Long accountId;

    @NotNull(message = "Hisse senedi ID boş olamaz")
    private Long stockId;

    @NotBlank(message = "Hisse senedi sembolü boş olamaz")
    private String stockSymbol;

    @NotBlank(message = "Hisse senedi adı boş olamaz")
    private String stockName;

    @NotNull(message = "Miktar boş olamaz")
    @Positive(message = "Miktar pozitif olmalıdır")
    private Integer quantity;

    @NotNull(message = "Fiyat boş olamaz")
    @Positive(message = "Fiyat pozitif olmalıdır")
    private BigDecimal price;

    @NotNull(message = "Toplam tutar boş olamaz")
    private BigDecimal totalAmount;

    @NotNull(message = "Net tutar boş olamaz")
    private BigDecimal netAmount;

    @NotNull(message = "Emir tipi boş olamaz")
    private OrderType orderType;

    private ExecutionType executionType;

    private OrderStatus status;
}
