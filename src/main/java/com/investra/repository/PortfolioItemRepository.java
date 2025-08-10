package com.investra.repository;

import com.investra.entity.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

       @Query("SELECT pi FROM PortfolioItem pi " +
                     "JOIN pi.portfolio p " +
                     "WHERE p.client.id = :clientId " +
                     " AND pi.quantity > 0")
       List<PortfolioItem> findByClientId(@Param("clientId") Long clientId);

       @Query("SELECT pi FROM PortfolioItem pi " +
                     "JOIN pi.portfolio p " +
                     "WHERE p.client.id = :clientId " +
                     " AND pi.stock.id = :stockId " +
                     " AND pi.quantity > 0")
       Optional<PortfolioItem> findByClientIdAndStockId(Long clientId, Long stockId);

       /**
        * Portföy pozisyonu olan müşteri ID'lerini getir
        */
       @Query("SELECT DISTINCT p.client.id FROM PortfolioItem pi " +
                     "JOIN pi.portfolio p " +
                     "WHERE pi.quantity > 0")
       List<Long> findClientIdsWithPortfolioItems();

       /**
        * Belirli bir tarihteki portföy pozisyonlarını getir
        */
       @Query("SELECT pi FROM PortfolioItem pi " +
                     "JOIN pi.portfolio p " +
                     "WHERE p.client.id = :clientId " +
                     " AND pi.quantity > 0 " +
                     " AND pi.lastUpdated >= :startOfDay " +
                     " AND pi.lastUpdated < :endOfDay")
       List<PortfolioItem> findByClientIdAndDate(@Param("clientId") Long clientId,
                     @Param("startOfDay") LocalDate startOfDay,
                     @Param("endOfDay") LocalDate endOfDay);

       /**
        * Belirli bir tarihten sonra güncellenen portföy pozisyonlarını getir
        */
       @Query("SELECT pi FROM PortfolioItem pi " +
                     "JOIN pi.portfolio p " +
                     "WHERE p.client.id = :clientId " +
                     " AND pi.quantity > 0 " +
                     " AND pi.lastUpdated >= :date")
       List<PortfolioItem> findByClientIdAndDateAfter(@Param("clientId") Long clientId,
                     @Param("date") LocalDate date);
}
