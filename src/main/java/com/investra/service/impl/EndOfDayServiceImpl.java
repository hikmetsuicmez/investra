package com.investra.service.impl;

import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.StockPositionResponse;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.dtos.response.infina.StockPriceResponse.StockPrice;
import com.investra.entity.*;
import com.investra.exception.BusinessException;
import com.investra.repository.*;
import com.investra.service.EndOfDayService;
import com.investra.service.InfinaApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndOfDayServiceImpl implements EndOfDayService {

    private final InfinaApiService infinaApiService;
    private final StockRepository stockRepository;
    private final StockDailyPriceRepository stockDailyPriceRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioDailyValuationRepository portfolioDailyValuationRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean fetchLatestClosingPrices() {
        try {
            LocalDate today = LocalDate.now();
            log.info("Günlük kapanış fiyatları alınıyor: {}", today);

            // API'den tüm hisse fiyatlarını al
            List<StockPrice> prices = infinaApiService.getAllStockPrices();

            if (prices.isEmpty()) {
                log.error("API'den hisse fiyatları alınamadı");
                return false;
            }

            // Her bir fiyat için günlük fiyat kaydı oluştur
            List<StockDailyPrice> dailyPrices = new ArrayList<>();

            for (StockPrice price : prices) {
                String stockCode = price.getStockCode();
                if (stockCode == null) continue;

                // Hisseyi bul
                Optional<Stock> stockOpt = stockRepository.findByCode(stockCode);
                if (stockOpt.isEmpty()) continue;

                Stock stock = stockOpt.get();

                // Günlük fiyatı kontrol et, varsa güncelle yoksa yeni oluştur
                StockDailyPrice dailyPrice = stockDailyPriceRepository
                    .findByStockIdAndPriceDate(stock.getId(), today);

                if (dailyPrice == null) {
                    dailyPrice = new StockDailyPrice();
                    dailyPrice.setStock(stock);
                    dailyPrice.setPriceDate(today);
                }

                dailyPrice.setClosePrice(price.getPrice());
                dailyPrice.setOpenPrice(price.getOpenPrice());
                dailyPrice.setHighPrice(price.getHighPrice());
                dailyPrice.setLowPrice(price.getLowPrice());

                // Değişim yüzdesini hesapla - önceki gün fiyatı varsa
                // Bu örnek için basitleştirilmiş, gerçekte önceki gün fiyatı ile karşılaştırılmalı
                if (price.getOpenPrice() != null && price.getOpenPrice().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal change = price.getPrice().subtract(price.getOpenPrice())
                        .divide(price.getOpenPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                    dailyPrice.setChangePercentage(change);
                }

                dailyPrice.setOfficialClose(true); // Resmi kapanış fiyatı

                dailyPrices.add(dailyPrice);

                // Hisse tablosundaki fiyatı da güncelle
                stock.setPrice(price.getPrice());
                stockRepository.save(stock);
            }

            // Toplu kaydet
            stockDailyPriceRepository.saveAll(dailyPrices);

            log.info("Toplam {} hisse senedi için kapanış fiyatları güncellendi", dailyPrices.size());
            return true;
        } catch (Exception e) {
            log.error("Kapanış fiyatları güncellenirken hata: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void runEndOfDayValuation(String username) {
        LocalDate today = LocalDate.now();

        // 1. BİST verilerini kontrol et
        if (!arePricesUpdatedForToday(today)) {
            throw new BusinessException("Gün sonu fiyatları henüz güncellenmedi.");
        }

        // 2. Tüm aktif müşterileri getir
        List<Client> clients = clientRepository.findAllByIsActive(true);

        // 3. Her müşteri için değerleme yap
        for (Client client : clients) {
            calculateClientPortfolioValuation(client, today, username);
        }

        log.info("Gün sonu değerleme tamamlandı: {} müşteri için", clients.size());
    }

    @Override
    public EndOfDayStatusResponse getEndOfDayStatus() {
        LocalDate today = LocalDate.now();

        boolean pricesUpdated = arePricesUpdatedForToday(today);
        boolean valuationCompleted = portfolioDailyValuationRepository.existsByValuationDate(today);

        String statusMessage;
        if (!pricesUpdated) {
            statusMessage = "Kapanış fiyatları bekleniyor";
        } else if (!valuationCompleted) {
            statusMessage = "BİST kapanış fiyatları başarıyla yüklendi";
        } else {
            statusMessage = today + " tarihli değerleme tamamlanmıştır. Değişiklik yapılamaz.";
        }

        // Son güncelleme zamanını bul
        LocalDateTime lastUpdateTime = LocalDateTime.now(); // Varsayılan değer

        return EndOfDayStatusResponse.builder()
                .pricesUpdated(pricesUpdated)
                .valuationCompleted(valuationCompleted)
                .valuationDate(today)
                .lastPriceUpdateTime(lastUpdateTime)
                .statusMessage(statusMessage)
                .build();
    }

    @Override
    public ClientValuationResponse getClientValuation(Long clientId) {
        LocalDate today = LocalDate.now();

        // Müşteri bilgilerini getir
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı"));

        // Müşterinin değerlemesini getir
        List<PortfolioDailyValuation> valuations = portfolioDailyValuationRepository
                .findAllByClientIdAndValuationDate(clientId, today);

        if (valuations.isEmpty()) {
            throw new BusinessException("Bu müşteri için değerleme bulunamadı");
        }

        PortfolioDailyValuation valuation = valuations.get(0);

        // Müşterinin pozisyonlarını getir
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(clientId);
        List<StockPositionResponse> positions = new ArrayList<>();

        for (PortfolioItem item : portfolioItems) {
            Stock stock = item.getStock();

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository
                    .findByStockIdAndPriceDate(stock.getId(), today);

            if (latestPrice == null) continue;

            BigDecimal positionValue = latestPrice.getClosePrice()
                    .multiply(new BigDecimal(item.getQuantity()));

            BigDecimal costValue = item.getAvgPrice()
                    .multiply(new BigDecimal(item.getQuantity()));

            BigDecimal unrealizedPL = positionValue.subtract(costValue);

            BigDecimal changePercentage = BigDecimal.ZERO;
            if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                changePercentage = unrealizedPL.divide(costValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            positions.add(StockPositionResponse.builder()
                    .stockCode(stock.getCode())
                    .stockName(stock.getName())
                    .quantity(item.getQuantity())
                    .costPrice(item.getAvgPrice())
                    .currentPrice(latestPrice.getClosePrice())
                    .positionValue(positionValue)
                    .unrealizedProfitLoss(unrealizedPL)
                    .changePercentage(changePercentage)
                    .build());
        }

        return ClientValuationResponse.builder()
                .clientId(client.getId())
                .clientName(client.getFullName())
                .totalPortfolioValue(valuation.getTotalPortfolioValue())
                .unrealizedProfitLoss(valuation.getUnrealizedProfitLoss())
                .dailyChangePercentage(valuation.getDailyChangePercentage())
                .totalReturnPercentage(valuation.getTotalReturnPercentage())
                .valuationDate(valuation.getValuationDate())
                .positions(positions)
                .build();
    }

    @Override
    public List<ClientValuationResponse> getAllClientValuations() {
        LocalDate today = LocalDate.now();

        List<PortfolioDailyValuation> valuations = portfolioDailyValuationRepository
                .findAllByValuationDate(today);

        return valuations.stream()
                .map(valuation -> {
                    Client client = valuation.getClient();
                    return ClientValuationResponse.builder()
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .totalPortfolioValue(valuation.getTotalPortfolioValue())
                            .unrealizedProfitLoss(valuation.getUnrealizedProfitLoss())
                            .dailyChangePercentage(valuation.getDailyChangePercentage())
                            .totalReturnPercentage(valuation.getTotalReturnPercentage())
                            .valuationDate(valuation.getValuationDate())
                            .positions(new ArrayList<>()) // Pozisyonları doldurma (detaylı görünüm için ayrı API çağrısı yapılabilir)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<StockPriceResponse> getAllStockPrices(LocalDate date) {
        List<StockDailyPrice> prices = stockDailyPriceRepository.findAllByPriceDate(date);

        return prices.stream()
                .map(price -> {
                    Stock stock = price.getStock();
                    return StockPriceResponse.builder()
                            .stockCode(stock.getCode())
                            .closePrice(price.getClosePrice())
                            .companyName(stock.getName())
                            .sector(stock.getSector())
                            .highPrice(price.getHighPrice())
                            .lowPrice(price.getLowPrice())
                            .changePercentage(price.getChangePercentage())
                            .volume(price.getVolume())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean manuallyUpdateClosingPrices() {
        return fetchLatestClosingPrices();
    }

    private boolean arePricesUpdatedForToday(LocalDate date) {
        return stockDailyPriceRepository.existsByPriceDateAndOfficialClose(date, true);
    }

    private void calculateClientPortfolioValuation(Client client, LocalDate valuationDate, String username) {
        // Müşterinin mevcut portföyünü getir
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());

        if (portfolioItems.isEmpty()) {
            return; // Portföyü boş ise hesaplama yapma
        }

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal unrealizedProfitLoss = BigDecimal.ZERO;

        // Her bir hisse pozisyonu için değerleme yap
        for (PortfolioItem item : portfolioItems) {
            Stock stock = item.getStock();

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository.findByStockIdAndPriceDate(
                stock.getId(), valuationDate);

            if (latestPrice == null) {
                continue; // Fiyat yoksa hesaplama yapma
            }

            // A) Pozisyon değeri hesapla
            BigDecimal positionValue = latestPrice.getClosePrice()
                .multiply(new BigDecimal(item.getQuantity()));

            // C) Gerçekleşmemiş K/Z hesapla
            BigDecimal costValue = item.getAvgPrice()
                .multiply(new BigDecimal(item.getQuantity()));
            BigDecimal positionProfitLoss = positionValue.subtract(costValue);

            // Toplamları güncelle
            totalPortfolioValue = totalPortfolioValue.add(positionValue);
            unrealizedProfitLoss = unrealizedProfitLoss.add(positionProfitLoss);
        }

        // Önceki gün değerini getir
        BigDecimal previousDayValue = getPreviousDayValue(client.getId(), valuationDate);

        // D) Günlük değişim yüzdesi hesapla
        BigDecimal dailyChangePercentage = BigDecimal.ZERO;
        if (previousDayValue.compareTo(BigDecimal.ZERO) > 0) {
            dailyChangePercentage = totalPortfolioValue.subtract(previousDayValue)
                .divide(previousDayValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }

        // E) Toplam getiri yüzdesi hesapla
        BigDecimal initialInvestment = getInitialInvestment(client.getId());
        BigDecimal totalReturnPercentage = BigDecimal.ZERO;
        if (initialInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnPercentage = totalPortfolioValue.subtract(initialInvestment)
                .divide(initialInvestment, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }

        // Değerlemeyi kaydet
        PortfolioDailyValuation valuation = new PortfolioDailyValuation();
        valuation.setClient(client);
        valuation.setValuationDate(valuationDate);
        valuation.setTotalPortfolioValue(totalPortfolioValue);
        valuation.setUnrealizedProfitLoss(unrealizedProfitLoss);
        valuation.setDailyChangePercentage(dailyChangePercentage);
        valuation.setTotalReturnPercentage(totalReturnPercentage);
        valuation.setInitialInvestment(initialInvestment);
        valuation.setPreviousDayValue(previousDayValue);
        valuation.setLocked(true);
        valuation.setCreatedAt(LocalDateTime.now());
        valuation.setCreatedBy(userRepository.findByUsername(username).orElse(null));

        portfolioDailyValuationRepository.save(valuation);
    }

    private BigDecimal getPreviousDayValue(Long clientId, LocalDate currentDate) {
        // Önceki değerlemeyi bul
        List<PortfolioDailyValuation> previousValuations = portfolioDailyValuationRepository
                .findPreviousValuations(clientId, currentDate);

        if (!previousValuations.isEmpty()) {
            return previousValuations.get(0).getTotalPortfolioValue();
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal getInitialInvestment(Long clientId) {
        // Bu basitleştirilmiş bir yaklaşımdır
        // Gerçek hesaplama için, yatırım tarihçesi analiz edilmelidir

        BigDecimal total = BigDecimal.ZERO;
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(clientId);

        for (PortfolioItem item : portfolioItems) {
            BigDecimal itemCost = item.getAvgPrice()
                    .multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemCost);
        }

        return total;
    }
}
