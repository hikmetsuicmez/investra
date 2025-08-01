package com.investra.repository;

import com.investra.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    // Müşterinin sahip olduğu tüm hisse senetlerini getir
    @Query("SELECT pi FROM PortfolioItem pi " +
           "JOIN pi.portfolio p " +
           "WHERE p.client.id = :clientId AND pi.quantity > 0")
    List<PortfolioItem> findByClientId(@Param("clientId") Long clientId);

    // Müşterinin belirli bir hisse senedindeki pozisyonunu getir
    @Query("SELECT pi FROM PortfolioItem pi " +
           "JOIN pi.portfolio p " +
           "WHERE p.client.id = :clientId AND pi.stock.id = :stockId AND pi.quantity > 0")
    Optional<PortfolioItem> findByClientIdAndStockId(@Param("clientId") Long clientId, @Param("stockId") Long stockId);

    // Müşterinin belirli hesaptaki hisse senetlerini getir
    @Query("SELECT pi FROM PortfolioItem pi " +
           "JOIN pi.portfolio p " +
           "WHERE p.client.id = :clientId AND pi.account.id = :accountId AND pi.quantity > 0")
    List<PortfolioItem> findByClientIdAndAccountId(@Param("clientId") Long clientId, @Param("accountId") Long accountId);
}
