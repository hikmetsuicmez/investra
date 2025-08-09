package com.investra.repository;

import com.investra.entity.StockDailyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockDailyPriceRepository extends JpaRepository<StockDailyPrice, Long> {

    StockDailyPrice findByStockIdAndPriceDate(Long stockId, LocalDate priceDate);

    List<StockDailyPrice> findAllByPriceDate(LocalDate priceDate);

    @Query("SELECT sdp FROM StockDailyPrice sdp WHERE sdp.priceDate = :priceDate AND sdp.officialClose = true")
    List<StockDailyPrice> findAllOfficialClosePricesByDate(LocalDate priceDate);

    boolean existsByPriceDateAndOfficialClose(LocalDate priceDate, boolean officialClose);

    // Test amaçlı günlük fiyatları silmek için
    int deleteByPriceDate(LocalDate priceDate);
}
