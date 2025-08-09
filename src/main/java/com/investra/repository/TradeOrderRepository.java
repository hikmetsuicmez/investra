package com.investra.repository;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.TradeOrder;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import com.investra.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

        List<TradeOrder> findByUserOrderBySubmittedAtDesc(User user);

        List<TradeOrder> findByUserAndStatusOrderBySubmittedAtDesc(User user, OrderStatus status);

        List<TradeOrder> findByStatus(OrderStatus status);

        List<TradeOrder> findByStatusAndSettlementStatusAndSettlementDateBefore(
                        OrderStatus status, SettlementStatus settlementStatus, LocalDateTime settlementDate);

        List<TradeOrder> findByStatusAndSubmittedAtBefore(OrderStatus status, LocalDateTime expiryTime);

        List<TradeOrder> findByClient(Client client);

        List<TradeOrder> findByAccount(Account account);

        List<TradeOrder> findAllByClientId(Long clientId);

        // T+2 settlement için yeni metodlar
        List<TradeOrder> findByStatusAndSettlementStatusAndTradeDate(
                        OrderStatus status, SettlementStatus settlementStatus, LocalDate tradeDate);

        List<TradeOrder> findByStatusAndSettlementStatus(
                        OrderStatus status, SettlementStatus settlementStatus);

        List<TradeOrder> findBySettlementStatus(SettlementStatus settlementStatus);

        // T+2 tamamlanmış işlemler için yeni metod
        List<TradeOrder> findByClientAndStatusAndSettlementStatus(
                        Client client, OrderStatus status, SettlementStatus settlementStatus);

        // Müşterinin belirli durumdaki işlemlerini getir
        List<TradeOrder> findByClientAndStatus(Client client, OrderStatus status);

        /**
         * EXECUTED işlemi olan müşteri ID'lerini getir
         */
        @Query("SELECT DISTINCT t.client.id FROM TradeOrder t " +
                        "WHERE t.status = 'EXECUTED'")
        List<Long> findClientIdsWithExecutedTrades();
}
