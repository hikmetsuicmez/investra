package com.investra.service;

import com.investra.dtos.response.Response;
import com.investra.dtos.response.TradeOrderDTO;
import com.investra.entity.Account;
import com.investra.entity.TradeOrder;
import com.investra.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public interface TradeOrderService {

    void processWaitingOrders();
    void processPendingLimitOrder(TradeOrder order);
    void processWaitingOrder(TradeOrder order);
    void processCancelledOrder(TradeOrder order);
    void settleCompletedOrder(TradeOrder order);

    List<TradeOrderDTO> getAllOrdersByUser(String username);
    List<TradeOrderDTO> getOrdersByStatusAndUser(String username, OrderStatus status);
    Response<TradeOrderDTO> cancelOrder(Long orderId, String username);
    void updateAccountBalanceForBuyOrder(Account account, BigDecimal amount);
    void restoreAccountBalanceForCancelledBuyOrder(Account account, BigDecimal amount);
}
