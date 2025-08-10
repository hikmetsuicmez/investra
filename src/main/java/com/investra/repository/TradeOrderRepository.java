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

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

        List<TradeOrder> findByUserOrderBySubmittedAtDesc(User user);

        List<TradeOrder> findByUserAndStatusOrderBySubmittedAtDesc(User user, OrderStatus status);

        List<TradeOrder> findByStatus(OrderStatus status);

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

        /**
         * Belirli bir tarihteki işlemleri getir
         */
        List<TradeOrder> findByTradeDate(LocalDate tradeDate);

        /**
         * Belirli bir müşterinin belirli bir tarihteki işlemlerini getir
         */
        List<TradeOrder> findByClientAndTradeDate(Client client, LocalDate tradeDate);

        /**
         * Belirli bir müşterinin belirli bir tarihteki EXECUTED işlemlerini getir
         */
        List<TradeOrder> findByClientAndStatusAndTradeDate(Client client, OrderStatus status, LocalDate tradeDate);

        /**
         * Belirli bir tarih aralığındaki işlemleri getir
         */
        List<TradeOrder> findByTradeDateBetween(LocalDate startDate, LocalDate endDate);

        /**
         * Belirli bir müşterinin belirli bir tarih aralığındaki işlemlerini getir
         */
        List<TradeOrder> findByClientAndTradeDateBetween(Client client, LocalDate startDate, LocalDate endDate);
}
