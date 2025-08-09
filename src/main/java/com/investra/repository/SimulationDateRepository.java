package com.investra.repository;

import com.investra.entity.SimulationDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationDateRepository extends JpaRepository<SimulationDate, Long> {

    @Query("SELECT s FROM SimulationDate s ORDER BY s.lastUpdatedAt DESC LIMIT 1")
    Optional<SimulationDate> findCurrentSimulationDate();

    @Query("SELECT s FROM SimulationDate s WHERE s.id = (SELECT MAX(s2.id) FROM SimulationDate s2)")
    Optional<SimulationDate> findLatest();

    boolean existsByCurrentSimulationDate(java.time.LocalDate date);
}
