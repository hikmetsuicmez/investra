package com.investra.mapper;

import com.investra.dtos.response.TradeOrderDTO;
import com.investra.entity.TradeOrder;
import org.springframework.stereotype.Component;

@Component
public class TradeOrderMapper {


    public static TradeOrderDTO toDTO(TradeOrder order) {
        return TradeOrderDTO.builder()
                .id(order.getId())
                .clientId(order.getClient() != null ? order.getClient().getId() : null)
                .clientFullName(order.getClient() != null ? order.getClient().getFullName() : null)
                .stockCode(order.getStock() != null ? order.getStock().getCode() : null)
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .settlementStatus(order.getSettlementStatus())
                .status(order.getStatus().name())
                .createdAt(order.getSubmittedAt())
                .executedAt(order.getExecutedAt())
                .build();
    }
}
