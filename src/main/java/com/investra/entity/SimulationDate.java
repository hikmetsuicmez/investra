package com.investra.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "simulation_dates")
@Builder
@Entity
public class SimulationDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_simulation_date", nullable = false)
    private LocalDate currentSimulationDate;

    @Column(name = "initial_date", nullable = false)
    private LocalDate initialDate;

    @Column(name = "days_advanced", nullable = false)
    private Integer daysAdvanced;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Column(name = "description")
    private String description;

    // Yardımcı metodlar
    public void advanceDay() {
        this.currentSimulationDate = this.currentSimulationDate.plusDays(1);
        this.daysAdvanced = this.daysAdvanced + 1;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void resetToToday() {
        this.currentSimulationDate = LocalDate.now();
        this.initialDate = LocalDate.now();
        this.daysAdvanced = 0;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public boolean isAdvanced() {
        return this.daysAdvanced > 0;
    }

    public LocalDate getEffectiveTradeDate() {
        return this.currentSimulationDate;
    }

    public LocalDate getEffectiveSettlementDate(int settlementDays) {
        return this.currentSimulationDate.plusDays(settlementDays);
    }
}
