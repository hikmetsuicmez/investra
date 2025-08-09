package com.investra.service.impl;

import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.StockPositionResponse;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.dtos.response.infina.StockPriceResponse.StockPrice;
import com.investra.entity.*;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import com.investra.exception.BusinessException;
import com.investra.repository.*;
import com.investra.service.EndOfDayService;
import com.investra.service.InfinaApiService;
import com.investra.service.PortfolioService;
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
    private final TradeOrderRepository tradeOrderRepository;
    private final AccountRepository accountRepository;
    private final PortfolioService portfolioService;

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
                if (stockCode == null)
                    continue;

                // Hisseyi bul
                Optional<Stock> stockOpt = stockRepository.findByCode(stockCode);
                if (stockOpt.isEmpty())
                    continue;

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
                // Bu örnek için basitleştirilmiş, gerçekte önceki gün fiyatı ile
                // karşılaştırılmalı
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

        // 2. Sadece işlemi olan müşterileri getir (optimizasyon)
        List<Client> clientsWithActivity = getClientsWithActivity();
        log.info("Değerleme yapılacak müşteri sayısı: {} (işlemi olan müşteriler)", clientsWithActivity.size());

        if (clientsWithActivity.isEmpty()) {
            log.warn("Değerleme yapılacak müşteri bulunamadı - hiç işlem yok");
            return;
        }

        // 3. Her müşteri için değerleme yap
        int processedCount = 0;
        for (Client client : clientsWithActivity) {
            try {
                calculateClientPortfolioValuation(client, today, username);
                processedCount++;
            } catch (Exception e) {
                log.error("Müşteri {} için değerleme yapılırken hata: {}", client.getId(), e.getMessage());
                // Hata durumunda diğer müşterileri etkilememek için devam et
            }
        }

        log.info("Gün sonu değerleme tamamlandı: {} müşteri için değerleme yapıldı", processedCount);
    }

    /**
     * İşlemi olan müşterileri getir (portföy pozisyonu veya EXECUTED işlemi olan)
     */
    private List<Client> getClientsWithActivity() {
        log.info("İşlemi olan müşteriler aranıyor...");

        // 1. Portföy pozisyonu olan müşterileri getir
        List<Long> clientsWithPortfolio = portfolioItemRepository.findClientIdsWithPortfolioItems();
        log.info("Portföy pozisyonu olan müşteri sayısı: {}", clientsWithPortfolio.size());

        // 2. EXECUTED işlemi olan müşterileri getir
        List<Long> clientsWithTrades = tradeOrderRepository.findClientIdsWithExecutedTrades();
        log.info("EXECUTED işlemi olan müşteri sayısı: {}", clientsWithTrades.size());

        // 3. İki listeyi birleştir ve unique yap
        List<Long> allActiveClientIds = new ArrayList<>();
        allActiveClientIds.addAll(clientsWithPortfolio);
        allActiveClientIds.addAll(clientsWithTrades);

        // Duplicate'leri kaldır
        List<Long> uniqueClientIds = allActiveClientIds.stream()
                .distinct()
                .collect(Collectors.toList());

        log.info("Toplam işlemi olan müşteri sayısı: {}", uniqueClientIds.size());

        // 4. Client objelerini getir
        if (uniqueClientIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Client> clients = clientRepository.findAllById(uniqueClientIds);
        log.info("İşlemi olan müşteriler bulundu: {} adet", clients.size());

        return clients;
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
        log.info("=== MÜŞTERİ DEĞERLEME SORGULANIYOR ===");
        log.info("Müşteri ID: {}", clientId);

        LocalDate today = LocalDate.now();
        log.info("Bugünün tarihi: {}", today);

        // Müşteri bilgilerini getir
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı"));
        log.info("Müşteri bulundu: {}", client.getFullName());

        // Müşterinin işlemi var mı kontrol et
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(clientId);
        List<TradeOrder> executedTrades = tradeOrderRepository.findByClientAndStatus(
                client, OrderStatus.EXECUTED);

        // Settlement status'una göre filtrele
        List<TradeOrder> validTrades = executedTrades.stream()
                .filter(trade -> trade.getSettlementStatus() == SettlementStatus.PENDING ||
                        trade.getSettlementStatus() == SettlementStatus.T1 ||
                        trade.getSettlementStatus() == SettlementStatus.T2)
                .collect(Collectors.toList());

        log.info("Portföy pozisyonları: {} adet, Geçerli işlemler: {} adet",
                portfolioItems.size(), validTrades.size());

        // Eğer müşterinin hiç işlemi yoksa, sıfır değerli değerleme döndür
        if (portfolioItems.isEmpty() && validTrades.isEmpty()) {
            log.warn("Müşteri {} için hiç işlem bulunamadı - sıfır değerli değerleme döndürülüyor", clientId);

            return ClientValuationResponse.builder()
                    .clientId(client.getId())
                    .clientName(client.getFullName())
                    .totalPortfolioValue(BigDecimal.ZERO)
                    .unrealizedProfitLoss(BigDecimal.ZERO)
                    .dailyChangePercentage(BigDecimal.ZERO)
                    .totalReturnPercentage(BigDecimal.ZERO)
                    .valuationDate(today)
                    .positions(new ArrayList<>())
                    .build();
        }

        // Müşterinin değerlemesini getir
        List<PortfolioDailyValuation> valuations = portfolioDailyValuationRepository
                .findAllByClientIdAndValuationDate(clientId, today);
        log.info("Değerleme kayıtları bulundu: {} adet", valuations.size());

        PortfolioDailyValuation valuation;
        if (valuations.isEmpty()) {
            log.warn("Bu müşteri için değerleme bulunamadı - değerleme yapılacak. Müşteri ID: {}, Tarih: {}", clientId,
                    today);

            // Değerleme yoksa, değerleme yap
            try {
                calculateClientPortfolioValuation(client, today, "system");

                // Tekrar değerlemeyi getir
                valuations = portfolioDailyValuationRepository
                        .findAllByClientIdAndValuationDate(clientId, today);

                if (valuations.isEmpty()) {
                    log.error("Değerleme yapıldıktan sonra hala değerleme bulunamadı");
                    throw new BusinessException("Bu müşteri için değerleme bulunamadı");
                }

                valuation = valuations.get(0);
                log.info("Değerleme başarıyla yapıldı ve alındı");
            } catch (Exception e) {
                log.error("Değerleme yapılırken hata: {}", e.getMessage());
                throw new BusinessException("Bu müşteri için değerleme yapılamadı: " + e.getMessage());
            }
        } else {
            valuation = valuations.get(0);
        }

        log.info("Değerleme detayları - Toplam değer: {}, K/Z: {}",
                valuation.getTotalPortfolioValue(), valuation.getUnrealizedProfitLoss());

        List<StockPositionResponse> positions = new ArrayList<>();

        // 1. Portföy pozisyonları
        log.info("=== PORTFÖY POZİSYONLARI İŞLENİYOR ===");
        for (PortfolioItem item : portfolioItems) {
            Stock stock = item.getStock();
            log.info("Portföy pozisyonu işleniyor: Hisse: {}, Miktar: {}", stock.getCode(), item.getQuantity());

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository
                    .findByStockIdAndPriceDate(stock.getId(), today);

            if (latestPrice == null) {
                log.warn("Hisse {} için kapanış fiyatı bulunamadı", stock.getCode());
                continue;
            }

            log.info("Hisse {} kapanış fiyatı: {}", stock.getCode(), latestPrice.getClosePrice());

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

            log.info("Pozisyon hesaplaması - Değer: {}, Maliyet: {}, K/Z: {}, Değişim: {}%",
                    positionValue, costValue, unrealizedPL, changePercentage);

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

        // 2. EXECUTED işlemler (henüz settle olmamış)
        log.info("=== EXECUTED İŞLEMLER İŞLENİYOR ===");
        for (TradeOrder trade : validTrades) {
            Stock stock = trade.getStock();
            log.info("EXECUTED işlem işleniyor: Hisse: {}, Miktar: {}, Fiyat: {}, Settlement: {}",
                    stock.getCode(), trade.getQuantity(), trade.getPrice(), trade.getSettlementStatus());

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository
                    .findByStockIdAndPriceDate(stock.getId(), today);

            if (latestPrice == null) {
                log.warn("Hisse {} için kapanış fiyatı bulunamadı", stock.getCode());
                continue;
            }

            log.info("Hisse {} kapanış fiyatı: {}", stock.getCode(), latestPrice.getClosePrice());

            BigDecimal positionValue = latestPrice.getClosePrice()
                    .multiply(new BigDecimal(trade.getQuantity()));

            BigDecimal costValue = trade.getPrice()
                    .multiply(new BigDecimal(trade.getQuantity()));

            BigDecimal unrealizedPL = positionValue.subtract(costValue);

            BigDecimal changePercentage = BigDecimal.ZERO;
            if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                changePercentage = unrealizedPL.divide(costValue, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            log.info("İşlem pozisyon hesaplaması - Değer: {}, Maliyet: {}, K/Z: {}, Değişim: {}%",
                    positionValue, costValue, unrealizedPL, changePercentage);

            positions.add(StockPositionResponse.builder()
                    .stockCode(stock.getCode())
                    .stockName(stock.getName())
                    .quantity(trade.getQuantity())
                    .costPrice(trade.getPrice())
                    .currentPrice(latestPrice.getClosePrice())
                    .positionValue(positionValue)
                    .unrealizedProfitLoss(unrealizedPL)
                    .changePercentage(changePercentage)
                    .build());
        }

        log.info("Toplam pozisyon sayısı: {}", positions.size());

        ClientValuationResponse response = ClientValuationResponse.builder()
                .clientId(client.getId())
                .clientName(client.getFullName())
                .totalPortfolioValue(valuation.getTotalPortfolioValue())
                .unrealizedProfitLoss(valuation.getUnrealizedProfitLoss())
                .dailyChangePercentage(valuation.getDailyChangePercentage())
                .totalReturnPercentage(valuation.getTotalReturnPercentage())
                .valuationDate(valuation.getValuationDate())
                .positions(positions) // Boş olsa bile pozisyonları döndür
                .build();

        log.info("=== MÜŞTERİ DEĞERLEME YANITI HAZIRLANDI ===");
        log.info("Toplam portföy değeri: {}, Pozisyon sayısı: {}",
                response.getTotalPortfolioValue(), response.getPositions().size());

        return response;
    }

    @Override
    public List<ClientValuationResponse> getAllClientValuations() {
        log.info("=== TÜM MÜŞTERİ DEĞERLEMELERİ SORGULANIYOR ===");
        LocalDate today = LocalDate.now();
        log.info("Bugünün tarihi: {}", today);

        List<PortfolioDailyValuation> valuations = portfolioDailyValuationRepository
                .findAllByValuationDate(today);
        log.info("Toplam değerleme kaydı bulundu: {} adet", valuations.size());

        // Eğer hiç değerleme yoksa, sadece işlemi olan müşteriler için değerleme yap
        if (valuations.isEmpty()) {
            log.warn("Hiç değerleme bulunamadı - işlemi olan müşteriler için değerleme yapılacak");
            try {
                runEndOfDayValuation("system");

                // Tekrar değerlemeleri getir
                valuations = portfolioDailyValuationRepository
                        .findAllByValuationDate(today);
                log.info("Değerleme yapıldıktan sonra toplam değerleme kaydı: {} adet", valuations.size());
            } catch (Exception e) {
                log.error("Toplu değerleme yapılırken hata: {}", e.getMessage());
                return new ArrayList<>(); // Hata durumunda boş liste döndür
            }
        }

        // Sadece işlemi olan müşterilerin değerlemelerini filtrele
        List<ClientValuationResponse> responses = new ArrayList<>();

        for (PortfolioDailyValuation valuation : valuations) {
            Client client = valuation.getClient();

            // Bu müşterinin işlemi var mı kontrol et
            List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());
            List<TradeOrder> executedTrades = tradeOrderRepository.findByClientAndStatus(
                    client, OrderStatus.EXECUTED);

            // Settlement status'una göre filtrele
            List<TradeOrder> validTrades = executedTrades.stream()
                    .filter(trade -> trade.getSettlementStatus() == SettlementStatus.PENDING ||
                            trade.getSettlementStatus() == SettlementStatus.T1 ||
                            trade.getSettlementStatus() == SettlementStatus.T2)
                    .collect(Collectors.toList());

            // Eğer müşterinin hiç işlemi yoksa, değerlemeyi atla
            if (portfolioItems.isEmpty() && validTrades.isEmpty()) {
                log.debug("Müşteri {} için işlem bulunamadı - değerleme atlanıyor", client.getId());
                continue;
            }

            log.info("Müşteri değerlemesi işleniyor: ID: {}, Ad: {}, Toplam değer: {}",
                    client.getId(), client.getFullName(), valuation.getTotalPortfolioValue());

            List<StockPositionResponse> positions = new ArrayList<>();

            // Portföy pozisyonları
            for (PortfolioItem item : portfolioItems) {
                Stock stock = item.getStock();
                StockDailyPrice latestPrice = stockDailyPriceRepository
                        .findByStockIdAndPriceDate(stock.getId(), today);

                if (latestPrice == null) {
                    log.warn("Hisse {} için {} tarihli kapanış fiyatı bulunamadı",
                            stock.getCode(), today);
                    continue;
                }

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

            // EXECUTED işlemler (henüz settle olmamış)
            for (TradeOrder trade : validTrades) {
                Stock stock = trade.getStock();
                StockDailyPrice latestPrice = stockDailyPriceRepository
                        .findByStockIdAndPriceDate(stock.getId(), today);

                if (latestPrice == null) {
                    log.warn("Hisse {} için {} tarihli kapanış fiyatı bulunamadı",
                            stock.getCode(), today);
                    continue;
                }

                BigDecimal positionValue = latestPrice.getClosePrice()
                        .multiply(new BigDecimal(trade.getQuantity()));

                BigDecimal costValue = trade.getPrice()
                        .multiply(new BigDecimal(trade.getQuantity()));

                BigDecimal unrealizedPL = positionValue.subtract(costValue);

                BigDecimal changePercentage = BigDecimal.ZERO;
                if (costValue.compareTo(BigDecimal.ZERO) > 0) {
                    changePercentage = unrealizedPL.divide(costValue, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                }

                positions.add(StockPositionResponse.builder()
                        .stockCode(stock.getCode())
                        .stockName(stock.getName())
                        .quantity(trade.getQuantity())
                        .costPrice(trade.getPrice())
                        .currentPrice(latestPrice.getClosePrice())
                        .positionValue(positionValue)
                        .unrealizedProfitLoss(unrealizedPL)
                        .changePercentage(changePercentage)
                        .build());
            }

            ClientValuationResponse response = ClientValuationResponse.builder()
                    .clientId(client.getId())
                    .clientName(client.getFullName())
                    .totalPortfolioValue(valuation.getTotalPortfolioValue())
                    .unrealizedProfitLoss(valuation.getUnrealizedProfitLoss())
                    .dailyChangePercentage(valuation.getDailyChangePercentage())
                    .totalReturnPercentage(valuation.getTotalReturnPercentage())
                    .valuationDate(valuation.getValuationDate())
                    .positions(positions)
                    .build();

            responses.add(response);
        }

        log.info("=== TÜM MÜŞTERİ DEĞERLEMELERİ TAMAMLANDI ===");
        log.info("Toplam değerleme yanıtı: {} adet", responses.size());

        return responses;
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

    @Override
    @Transactional
    public boolean resetEndOfDayStatus() {
        try {
            LocalDate today = LocalDate.now();
            log.info("Test amaçlı gün sonu durumu sıfırlanıyor: {}", today);

            // Bugünkü fiyatları sil
            int deletedPrices = stockDailyPriceRepository.deleteByPriceDate(today);
            log.info("{} adet günlük fiyat kaydı silindi", deletedPrices);

            // Bugünkü değerlemeleri sil
            int deletedValuations = portfolioDailyValuationRepository.deleteByValuationDate(today);
            log.info("{} adet değerleme kaydı silindi", deletedValuations);

            log.info("Gün sonu durumu başarıyla sıfırlandı");
            return true;
        } catch (Exception e) {
            log.error("Gün sonu durumu sıfırlanırken hata: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void processT0ToT1Settlement() {
        LocalDate today = LocalDate.now();
        log.info("T+0 işlemleri T+1'e geçiriliyor: {}", today);

        // Bugün yapılan işlemleri T+1'e geçir
        List<TradeOrder> pendingTrades = tradeOrderRepository
                .findByStatusAndSettlementStatusAndTradeDate(OrderStatus.EXECUTED, SettlementStatus.PENDING, today);

        for (TradeOrder trade : pendingTrades) {
            trade.setSettlementStatus(SettlementStatus.T1);
            trade.setSettlementDaysRemaining(1);
            tradeOrderRepository.save(trade);
            log.info("İşlem T+1'e geçirildi: {}", trade.getId());
        }

        log.info("Toplam {} işlem T+1'e geçirildi", pendingTrades.size());
    }

    @Override
    @Transactional
    public void processT1Settlement() {
        LocalDate today = LocalDate.now();
        log.info("T+1 işlemleri T+2'ye geçiriliyor: {}", today);

        // T+1'deki işlemleri T+2'ye geçir
        List<TradeOrder> t1Trades = tradeOrderRepository
                .findByStatusAndSettlementStatus(OrderStatus.EXECUTED, SettlementStatus.T1);

        for (TradeOrder trade : t1Trades) {
            trade.setSettlementStatus(SettlementStatus.T2);
            trade.setSettlementDaysRemaining(0);
            tradeOrderRepository.save(trade);
            log.info("İşlem T+2'ye geçirildi: {}", trade.getId());
        }

        log.info("Toplam {} işlem T+2'ye geçirildi", t1Trades.size());
    }

    @Override
    @Transactional
    public void processT2Settlement() {
        LocalDate today = LocalDate.now();
        log.info("T+2 işlemleri tamamlanıyor: {}", today);

        List<TradeOrder> t2Trades = tradeOrderRepository
                .findByStatusAndSettlementStatus(OrderStatus.EXECUTED, SettlementStatus.T2);

        log.info("Bulunan T+2 işlem sayısı: {}", t2Trades.size());

        for (TradeOrder trade : t2Trades) {
            log.info("İşlem başlatılıyor: ID={}, Status={}, SettlementStatus={}",
                    trade.getId(), trade.getStatus(), trade.getSettlementStatus());

            try {
                transferStockToBuyer(trade);
                log.info("Hisse transferi OK: {}", trade.getId());

                transferMoneyToSeller(trade);
                log.info("Para transferi OK: {}", trade.getId());

                trade.setSettlementStatus(SettlementStatus.COMPLETED);
                trade.setSettledAt(LocalDateTime.now());

                log.info("Kayıt ediliyor: {}", trade.getId());
                tradeOrderRepository.save(trade);

                log.info("İşlem tamamlandı: {}", trade.getId());
            } catch (Exception e) {
                log.error("HATA - İşlem: {}, Mesaj: {}", trade.getId(), e.getMessage(), e);
                throw e;
            }
        }

        log.info("Toplam {} işlem tamamlandı", t2Trades.size());
    }

    @Override
    @Transactional
    public void processAllT2SettlementSteps() {
        LocalDate today = LocalDate.now();
        log.info("Tüm T+2 settlement adımları başlatılıyor: {}", today);

        // 1. T+0 işlemleri T+1'e geçir
        processT0ToT1Settlement();

        // 2. T+1 işlemleri T+2'ye geçir
        processT1Settlement();

        // 3. T+2 işlemleri tamamla
        processT2Settlement();

        log.info("Tüm T+2 settlement adımları tamamlandı: {}", today);
    }

    private boolean arePricesUpdatedForToday(LocalDate date) {
        return stockDailyPriceRepository.existsByPriceDateAndOfficialClose(date, true);
    }

    private void calculateClientPortfolioValuation(Client client, LocalDate valuationDate, String username) {
        log.info("=== MÜŞTERİ DEĞERLEME BAŞLATILIYOR ===");
        log.info("Müşteri ID: {}, Müşteri Adı: {}, Değerleme Tarihi: {}",
                client.getId(), client.getFullName(), valuationDate);

        // Müşterinin mevcut portföyünü getir
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());
        log.info("Portföy pozisyonları bulundu: {} adet", portfolioItems.size());

        // Tüm EXECUTED durumundaki işlemleri getir (PENDING, T1, T2, COMPLETED)
        List<TradeOrder> executedTrades = tradeOrderRepository.findByClientAndStatus(
                client, OrderStatus.EXECUTED);
        log.info("EXECUTED işlemler bulundu: {} adet", executedTrades.size());

        // Settlement status'una göre filtrele
        List<TradeOrder> validTrades = executedTrades.stream()
                .filter(trade -> trade.getSettlementStatus() == SettlementStatus.PENDING ||
                        trade.getSettlementStatus() == SettlementStatus.T1 ||
                        trade.getSettlementStatus() == SettlementStatus.T2)
                .collect(Collectors.toList());
        log.info("Geçerli settlement status'undaki işlemler: {} adet", validTrades.size());

        // İşlem detaylarını logla
        for (TradeOrder trade : validTrades) {
            log.info("İşlem ID: {}, Hisse: {}, Miktar: {}, Fiyat: {}, Durum: {}, Settlement: {}",
                    trade.getId(), trade.getStock().getCode(), trade.getQuantity(),
                    trade.getPrice(), trade.getStatus(), trade.getSettlementStatus());
        }

        // Eğer müşterinin hiç işlemi yoksa, değerleme yapma
        if (portfolioItems.isEmpty() && validTrades.isEmpty()) {
            log.warn("Müşteri {} için portföy ve geçerli işlem bulunamadı - değerleme yapılmayacak",
                    client.getId());
            return; // Değerleme yapma, sıfır değerli kayıt oluşturma
        }

        // Eğer sadece portföy boş ama geçerli EXECUTED işlemler varsa, değerleme
        // yapılmalı
        if (portfolioItems.isEmpty() && !validTrades.isEmpty()) {
            log.info("Müşteri {} için portföy boş ama {} adet geçerli EXECUTED işlem var - değerleme yapılacak",
                    client.getId(), validTrades.size());
        }

        // Eğer sadece geçerli EXECUTED işlemler boş ama portföy varsa, değerleme
        // yapılmalı
        if (!portfolioItems.isEmpty() && validTrades.isEmpty()) {
            log.info(
                    "Müşteri {} için geçerli EXECUTED işlem yok ama {} adet portföy pozisyonu var - değerleme yapılacak",
                    client.getId(), portfolioItems.size());
        }

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal unrealizedProfitLoss = BigDecimal.ZERO;

        // 1. Mevcut portföy pozisyonları için değerleme yap
        log.info("=== PORTFÖY POZİSYONLARI DEĞERLENİYOR ===");
        for (PortfolioItem item : portfolioItems) {
            Stock stock = item.getStock();
            log.info("Portföy pozisyonu: Hisse: {}, Miktar: {}, Ortalama Fiyat: {}",
                    stock.getCode(), item.getQuantity(), item.getAvgPrice());

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository.findByStockIdAndPriceDate(
                    stock.getId(), valuationDate);

            if (latestPrice == null) {
                log.warn("Hisse {} için {} tarihli kapanış fiyatı bulunamadı",
                        stock.getCode(), valuationDate);
                continue; // Fiyat yoksa hesaplama yapma
            }

            log.info("Hisse {} için kapanış fiyatı: {}", stock.getCode(), latestPrice.getClosePrice());

            // A) Pozisyon değeri hesapla
            BigDecimal positionValue = latestPrice.getClosePrice()
                    .multiply(new BigDecimal(item.getQuantity()));

            // C) Gerçekleşmemiş K/Z hesapla
            BigDecimal costValue = item.getAvgPrice()
                    .multiply(new BigDecimal(item.getQuantity()));
            BigDecimal positionProfitLoss = positionValue.subtract(costValue);

            log.info("Pozisyon değeri: {}, Maliyet: {}, K/Z: {}",
                    positionValue, costValue, positionProfitLoss);

            // Toplamları güncelle
            totalPortfolioValue = totalPortfolioValue.add(positionValue);
            unrealizedProfitLoss = unrealizedProfitLoss.add(positionProfitLoss);
        }

        // 2. EXECUTED durumundaki işlemler için değerleme yap
        log.info("=== EXECUTED İŞLEMLER DEĞERLENİYOR ===");
        for (TradeOrder trade : validTrades) {
            Stock stock = trade.getStock();
            log.info("EXECUTED işlem: Hisse: {}, Miktar: {}, İşlem Fiyatı: {}, Settlement: {}",
                    stock.getCode(), trade.getQuantity(), trade.getPrice(), trade.getSettlementStatus());

            // Güncel kapanış fiyatını al
            StockDailyPrice latestPrice = stockDailyPriceRepository.findByStockIdAndPriceDate(
                    stock.getId(), valuationDate);

            if (latestPrice == null) {
                log.warn("Hisse {} için {} tarihli kapanış fiyatı bulunamadı",
                        stock.getCode(), valuationDate);
                continue; // Fiyat yoksa hesaplama yapma
            }

            log.info("Hisse {} için kapanış fiyatı: {}", stock.getCode(), latestPrice.getClosePrice());

            // EXECUTED işlemler için pozisyon değeri hesapla
            BigDecimal positionValue = latestPrice.getClosePrice()
                    .multiply(new BigDecimal(trade.getQuantity()));

            // EXECUTED işlemler için gerçekleşmemiş K/Z hesapla
            BigDecimal costValue = trade.getPrice()
                    .multiply(new BigDecimal(trade.getQuantity()));
            BigDecimal positionProfitLoss = positionValue.subtract(costValue);

            log.info("İşlem pozisyon değeri: {}, Maliyet: {}, K/Z: {}",
                    positionValue, costValue, positionProfitLoss);

            // Toplamları güncelle
            totalPortfolioValue = totalPortfolioValue.add(positionValue);
            unrealizedProfitLoss = unrealizedProfitLoss.add(positionProfitLoss);
        }

        log.info("=== TOPLAM DEĞERLER ===");
        log.info("Toplam portföy değeri: {}", totalPortfolioValue);
        log.info("Toplam gerçekleşmemiş K/Z: {}", unrealizedProfitLoss);

        // Önceki gün değerini getir
        BigDecimal previousDayValue = getPreviousDayValue(client.getId(), valuationDate);
        log.info("Önceki gün değeri: {}", previousDayValue);

        // D) Günlük değişim yüzdesi hesapla
        BigDecimal dailyChangePercentage = BigDecimal.ZERO;
        if (previousDayValue.compareTo(BigDecimal.ZERO) > 0) {
            dailyChangePercentage = totalPortfolioValue.subtract(previousDayValue)
                    .divide(previousDayValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        log.info("Günlük değişim yüzdesi: {}%", dailyChangePercentage);

        // E) Toplam getiri yüzdesi hesapla
        BigDecimal initialInvestment = getInitialInvestment(client.getId());
        BigDecimal totalReturnPercentage = BigDecimal.ZERO;
        if (initialInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalReturnPercentage = totalPortfolioValue.subtract(initialInvestment)
                    .divide(initialInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        log.info("Başlangıç yatırımı: {}, Toplam getiri yüzdesi: {}%",
                initialInvestment, totalReturnPercentage);

        // Değerlemeyi kaydet (sadece işlemi olan müşteriler için)
        PortfolioDailyValuation valuation = new PortfolioDailyValuation();
        valuation.setClient(client);
        valuation.setValuationDate(valuationDate);
        valuation.setTotalPortfolioValue(totalPortfolioValue);
        valuation.setUnrealizedProfitLoss(unrealizedProfitLoss);
        valuation.setDailyChangePercentage(dailyChangePercentage);
        valuation.setTotalReturnPercentage(totalReturnPercentage);
        valuation.setPreviousDayValue(previousDayValue);
        valuation.setInitialInvestment(initialInvestment);
        valuation.setLocked(false);
        valuation.setCreatedAt(LocalDateTime.now());

        // Kullanıcı bilgisini ekle
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
        valuation.setCreatedBy(user);

        PortfolioDailyValuation savedValuation = portfolioDailyValuationRepository.save(valuation);
        log.info("Değerleme kaydedildi. ID: {}", savedValuation.getId());

        log.info("=== MÜŞTERİ DEĞERLEME TAMAMLANDI ===");
        log.info("Müşteri {} için değerleme tamamlandı. Toplam değer: {}",
                client.getId(), totalPortfolioValue);
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

    private void transferStockToBuyer(TradeOrder trade) {
        // Alış işlemi ise portföye hisse ekle
        if (trade.getOrderType() == OrderType.BUY) {
            portfolioService.updatePortfolioAfterSettlement(trade);
        }
    }

    private void transferMoneyToSeller(TradeOrder trade) {
        Account account = trade.getAccount();

        if (trade.getOrderType() == OrderType.BUY) {
            // Alış işlemi: Balance'ı availableBalance seviyesine eşitle
            account.setBalance(account.getAvailableBalance());
        } else if (trade.getOrderType() == OrderType.SELL) {
            // Satış işlemi: Hem balance hem availableBalance artırılır
            account.setBalance(account.getBalance().add(trade.getNetAmount()));
            account.setAvailableBalance(account.getAvailableBalance().add(trade.getNetAmount()));
        }

        accountRepository.save(account);
    }
}
