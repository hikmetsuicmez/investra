package com.investra.dtos.request;

import com.investra.enums.ExecutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBuyOrderRequest {
    @NotNull(message = "Client ID cannot be null")
    private Long clientId;

    @NotNull(message = "Account ID cannot be null")
    private Long accountId;

    @NotNull(message = "Stock ID cannot be null")
    private Long stockId;

    @NotNull(message = "Execution type cannot be null")
    private ExecutionType executionType;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // LIMIT emri için kullanılır, MARKET emri için 0 olabilir
    private Double price;

    // Execute işlemi için previewId bilgisi
    private String previewId;
}
