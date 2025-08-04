package com.investra.repository;

import com.investra.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock,Long> {


    Optional<Stock> findByCode(String code);

    List<Stock> findByIsActiveTrue();
}
