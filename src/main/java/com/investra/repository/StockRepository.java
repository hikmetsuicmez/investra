package com.investra.repository;

import com.investra.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock,Long> {

    // Hisse senedi koduna göre arama yapar
    Optional<Stock> findByCode(String code);

    // Sembol adına göre arama yapar
    Optional<Stock> findBySymbol(String symbol);

    // İsim içeriğine göre hisse senetlerini arar
    List<Stock> findByNameContainingIgnoreCase(String name);

    // Sektöre göre hisse senetlerini arar
    List<Stock> findBySectorContainingIgnoreCase(String sector);

    // Aktif olan tüm hisse senetlerini getirir
    List<Stock> findByIsActiveTrue();
}
