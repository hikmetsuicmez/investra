package com.investra.service;

import com.investra.entity.SimulationDate;
import java.time.LocalDate;

public interface SimulationDateService {

    /**
     * Mevcut simülasyon tarihini getirir
     */
    LocalDate getCurrentSimulationDate();

    /**
     * Simülasyon tarihini bir gün ilerletir (haftasonu kontrolü ile)
     */
    SimulationDate advanceSimulationDay(String username);

    /**
     * Simülasyon tarihini bugüne sıfırlar
     */
    SimulationDate resetSimulationDate(String username);

    /**
     * Simülasyon durumunu getirir
     */
    SimulationDate getSimulationStatus();

    /**
     * Kaç gün ilerlendiğini getirir
     */
    Integer getDaysAdvanced();

    /**
     * Simülasyon tarihine göre settlement tarihi hesaplar
     */
    LocalDate calculateSettlementDate(int settlementDays);

    /**
     * Simülasyon sistemi başlatılıp başlatılmadığını kontrol eder
     */
    boolean isSimulationInitialized();

    /**
     * Simülasyon sistemini başlatır
     */
    SimulationDate initializeSimulation(String username);

    /**
     * Mevcut simülasyon tarihi haftasonu mu kontrol eder
     */
    boolean isWeekend();

    /**
     * Borsa açık mı kontrol eder
     */
    boolean isMarketOpen();
}