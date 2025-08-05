package com.investra.mapper;

import com.investra.dtos.OrderCreateDto;
import com.investra.dtos.OrderDTO;
import com.investra.entity.Order;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(OrderCreateDto dto, User user) {
        return Order.builder()
                .user(user)
                .stockSymbol(dto.getStockSymbol())
                .stockName(dto.getStockName())
                .quantity(dto.getQuantity())
                .price(dto.getPrice())
                .orderType(dto.getOrderType())
                .status(OrderStatus.PENDING)
                .tradeOrderId(dto.getTradeOrderId())  // TradeOrder ID eklendi
                .createdAt(LocalDateTime.now())
                .build();
    }

    public OrderDTO toDto(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .stockSymbol(order.getStockSymbol())
                .stockName(order.getStockName())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .settlementStatus(order.getSettlementStatus())  // SettlementStatus eklendi
                .tradeOrderId(order.getTradeOrderId())  // TradeOrder ID eklendi
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .settlementDate(order.getSettlementDate())
                .fundsReserved(order.isFundsReserved())
                .expirationDate(order.getExpirationDate())
                .totalAmount(order.getTotalAmount())
                .transactionReference(order.getTransactionReference())
                .portfolioUpdated(order.isPortfolioUpdated())  // portfolioUpdated eklendi
                .build();
    }

    public List<OrderDTO> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
