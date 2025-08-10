package com.investra.service.impl;

import com.investra.entity.SimulationDate;
import com.investra.repository.SimulationDateRepository;
import com.investra.service.SimulationDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SimulationDateServiceImpl implements SimulationDateService {

    private final SimulationDateRepository simulationDateRepository;

    @Override
    public LocalDate getCurrentSimulationDate() {
        SimulationDate current = getOrCreateSimulationDate();
        return current.getCurrentSimulationDate();
    }

    @Override
    public SimulationDate advanceSimulationDay(String username) {
        log.info("Simülasyon tarihi bir gün ilerletiliyor (sadece hafta içi), kullanıcı: {}", username);

        SimulationDate current = getOrCreateSimulationDate();
        LocalDate oldDate = current.getCurrentSimulationDate();

        // Bir sonraki hafta içi günü bul
        LocalDate newDate = getNextWeekday(oldDate);

        current.setCurrentSimulationDate(newDate);
        current.setDaysAdvanced(current.getDaysAdvanced() + 1); // Mantıksal gün sayısı
        current.setUpdatedBy(username);
        current.setDescription("Gün sonu işlemi ile tarih ilerletildi (haftasonu atlanarak)");
        current.setLastUpdatedAt(java.time.LocalDateTime.now());

        SimulationDate saved = simulationDateRepository.save(current);
        log.info("Simülasyon tarihi güncellendi: {} -> {} (haftasonu atlandı)",
                oldDate, saved.getCurrentSimulationDate());

        return saved;
    }

    /**
     * Verilen tarihten sonraki ilk hafta içi günü bulur
     */
    private LocalDate getNextWeekday(LocalDate date) {
        LocalDate nextDate = date.plusDays(1);

        while (nextDate.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                nextDate.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            nextDate = nextDate.plusDays(1);
        }

        return nextDate;
    }

    @Override
    public SimulationDate resetSimulationDate(String username) {
        log.info("Simülasyon tarihi sıfırlanıyor, kullanıcı: {}", username);

        // Mevcut kaydı sil
        simulationDateRepository.deleteAll();

        // Yeni simülasyon başlat (haftasonu kontrolü ile)
        SimulationDate fresh = initializeSimulation(username);
        fresh.setDescription("Simülasyon tarihi manuel olarak sıfırlandı (haftasonu kontrolü ile)");

        SimulationDate saved = simulationDateRepository.save(fresh);
        log.info("Simülasyon tarihi sıfırlandı: {}", saved.getCurrentSimulationDate());

        return saved;
    }

    @Override
    public SimulationDate getSimulationStatus() {
        return getOrCreateSimulationDate();
    }

    @Override
    public Integer getDaysAdvanced() {
        SimulationDate current = getOrCreateSimulationDate();
        return current.getDaysAdvanced();
    }

    @Override
    public LocalDate calculateSettlementDate(int settlementDays) {
        LocalDate currentDate = getCurrentSimulationDate();
        return currentDate.plusDays(settlementDays);
    }

    @Override
    public boolean isSimulationInitialized() {
        return simulationDateRepository.findCurrentSimulationDate().isPresent();
    }

    @Override
    public SimulationDate initializeSimulation(String username) {
        log.info("Simülasyon sistemi başlatılıyor, kullanıcı: {}", username);

        LocalDate today = LocalDate.now();

        // Eğer bugün haftasonuysa, en yakın hafta içi günü kullan
        LocalDate startDate = today;
        if (today.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                today.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            startDate = getNextWeekday(today); // Bugünden sonraki hafta içi
            log.info("Bugün haftasonu, simülasyon hafta içi günü ile başlatılıyor: {} -> {}", today, startDate);
        }

        SimulationDate simulation = SimulationDate.builder()
                .currentSimulationDate(startDate)
                .initialDate(startDate)
                .daysAdvanced(0)
                .lastUpdatedAt(LocalDateTime.now())
                .updatedBy(username)
                .description("Simülasyon sistemi başlatıldı (haftasonu atlanarak)")
                .build();

        SimulationDate saved = simulationDateRepository.save(simulation);
        log.info("Simülasyon sistemi başlatıldı: {}", saved.getCurrentSimulationDate());

        return saved;
    }

    @Override
    public boolean isWeekend() {
        LocalDate currentDate = getCurrentSimulationDate();
        java.time.DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
    }

    @Override
    public boolean isMarketOpen() {
        return !isWeekend();
    }

    private SimulationDate getOrCreateSimulationDate() {
        return simulationDateRepository.findCurrentSimulationDate()
                .orElseGet(() -> {
                    log.info("Simülasyon tarihi bulunamadı, yeni oluşturuluyor");
                    return initializeSimulation("SYSTEM");
                });
    }
}
