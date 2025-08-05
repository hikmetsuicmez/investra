package com.investra.repository;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.TradeOrder;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import com.investra.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long>{

    List<TradeOrder> findByUserOrderBySubmittedAtDesc(User user);

    List<TradeOrder> findByUserAndStatusOrderBySubmittedAtDesc(User user, OrderStatus status);

    List<TradeOrder> findByStatus(OrderStatus status);

    List<TradeOrder> findByStatusAndSettlementStatusAndSettlementDateBefore(
            OrderStatus status, SettlementStatus settlementStatus, LocalDateTime settlementDate);

    List<TradeOrder> findByStatusAndSubmittedAtBefore(OrderStatus status, LocalDateTime expiryTime);

    List<TradeOrder> findByClient(Client client);

    List<TradeOrder> findByAccount(Account account);

    List<TradeOrder> findAllByClientId(Long clientId);

}
