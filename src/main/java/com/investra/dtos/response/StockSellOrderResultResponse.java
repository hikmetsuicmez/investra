package com.investra.dtos.response;

import com.investra.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockSellOrderResultResponse {
    private Long orderId;
    private OrderStatus status;
    private String message;
    private LocalDateTime submittedAt;
    private boolean success;
}
