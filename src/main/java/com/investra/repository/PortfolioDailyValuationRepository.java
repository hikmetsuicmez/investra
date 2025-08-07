package com.investra.repository;

import com.investra.entity.PortfolioDailyValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioDailyValuationRepository extends JpaRepository<PortfolioDailyValuation, Long> {

    List<PortfolioDailyValuation> findAllByClientIdAndValuationDate(Long clientId, LocalDate valuationDate);

    List<PortfolioDailyValuation> findAllByValuationDate(LocalDate valuationDate);

    @Query("SELECT pdv FROM PortfolioDailyValuation pdv WHERE pdv.client.id = :clientId AND pdv.valuationDate < :date ORDER BY pdv.valuationDate DESC")
    List<PortfolioDailyValuation> findPreviousValuations(Long clientId, LocalDate date);

    @Query("SELECT CASE WHEN COUNT(pdv) > 0 THEN TRUE ELSE FALSE END FROM PortfolioDailyValuation pdv WHERE pdv.valuationDate = :date")
    boolean existsByValuationDate(LocalDate date);
}
