package com.investra.service;

import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.dtos.response.infina.StockPriceResponse;
import com.investra.entity.Stock;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final InfinaApiService infinaApiService;
    private final StockRepository stockRepository;

    // Kod-fiyat eşleşmesi için in-memory cache
    private final Map<String, BigDecimal> stockPriceCache = new ConcurrentHashMap<>();

    // üm hisse senetlerini getirir, önce veritabanından yoksa API'den çeker ve veritabanına kaydeder
    @Cacheable(value = "stocks", key = "'all_stocks'")
    public List<Stock> getAllStocks() {
        log.info("Cache MISS - Veritabanından hisse senetleri getiriliyor"); // Cache miss logı
        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            log.info("Veritabanında hisse senedi bulunamadı, API'den alınıyor");
            return refreshStocksFromApi();
        }

        log.info("Veritabanından {} hisse senedi alındı", stocks.size());
        return stocks;
    }

    /**
     * Belirli bir hisse senedini kodu ile getirir
     */
    // @Cacheable(value = "stocks", key = "#stockCode")
    public Optional<Stock> getStockByCode(String stockCode) {
        Optional<Stock> stockOpt = stockRepository.findByCode((stockCode));

        if (stockOpt.isEmpty()) {
            log.info("Veritabanında {} kodlu hisse senedi bulunamadı, API'den alınıyor", stockCode);
            refreshStocksFromApi();
            return stockRepository.findByCode(stockCode);
        }

        // Fiyatı güncel mi kontrol et
        updateStockPrice(stockOpt.get());

        return stockOpt;
    }

    /**
     * Hisse senedi fiyatını API'den günceller
     */
    public void updateStockPrice(Stock stock) {
        try {
            // Önce cache'den kontrol et
            BigDecimal cachedPrice = stockPriceCache.get(stock.getCode());
            if (cachedPrice != null) {
                stock.setPrice(cachedPrice);
                log.debug("Hisse senedi fiyatı cache'den alındı: {}", stock.getCode());
                return;
            }

            // Cache'de yoksa API'den al
            Optional<StockPriceResponse.StockPrice> priceOpt = infinaApiService.getStockPrice(stock.getCode());

            if (priceOpt.isPresent()) {
                StockPriceResponse.StockPrice price = priceOpt.get();
                stock.setPrice(price.getPrice());

                // Cache'e ekle
                stockPriceCache.put(stock.getCode(), price.getPrice());

                log.debug("Hisse senedi fiyatı API'den güncellendi: {}", stock.getCode());
            }
        } catch (Exception e) {
            log.error("Hisse senedi fiyatı güncellenirken hata: {}", e.getMessage());
        }
    }

    /**
     * Tüm hisse senedi tanımlarını API'den çekerek veritabanını günceller
     */
    @Transactional
    // @CacheEvict(value = "stocks", allEntries = true)
    public List<Stock> refreshStocksFromApi() {
        try {
            log.info("Hisse senedi verileri API'den yenileniyor");

            // Sadece hisse fiyatlarını API'den al
            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();

            log.info("API'den alınan fiyat sayısı: {}", prices.size());

            if (prices.isEmpty()) {
                log.warn("API'den hisse senedi fiyatları alınamadı");
                return stockRepository.findAll();
            }

            // Limit olmadan tüm hisseleri işle, sadece null olanları filtrele
            List<StockPriceResponse.StockPrice> filteredPrices = prices.stream()
                    .filter(price -> price.getStockCode() != null) // null olan kodları filtrele
                    .collect(Collectors.toList());

            log.info("İşlenecek hisse sayısı: {}", filteredPrices.size());

            // Önce tüm mevcut hisseleri al - bunlar korunacak
            List<Stock> existingStocks = stockRepository.findAll();
            log.info("Veritabanında mevcut hisse sayısı: {}", existingStocks.size());

            // Mevcut hisse kodlarını al
            List<String> existingCodes = existingStocks.stream()
                    .map(Stock::getCode)
                    .collect(Collectors.toList());

            // Cache'i güncelle - TÜM hisse fiyatları için
            stockPriceCache.clear();
            filteredPrices.stream()
                .forEach(price -> stockPriceCache.put(price.getStockCode(), price.getPrice()));

            // Mevcut hisselerin fiyatlarını güncelle
            existingStocks.forEach(stock -> {
                if (stock.getCode() != null) {
                    filteredPrices.stream()
                        .filter(price -> stock.getCode().equals(price.getStockCode()))
                        .findFirst()
                        .ifPresent(price -> stock.setPrice(price.getPrice()));
                }
            });

            // Yeni hisseleri entity'ye dönüştür (sadece veritabanında olmayanlar)
            List<Stock> newStocks = filteredPrices.stream()
                    .filter(price -> !existingCodes.contains(price.getStockCode())) // Sadece yeni kodları ekle
                    .map(price -> {
                        Stock stock = new Stock();
                        stock.setCode(price.getStockCode());
                        stock.setName(price.getStockCode()); // Geçici olarak kod ile doldur
                        stock.setIsActive(true);
                        stock.setPrice(price.getPrice());
                        // Varsayılan olarak FINANCE StockGroup'u atanıyor
                        stock.setGroup(com.investra.enums.StockGroup.FINANCE);

                        return stock;
                    })
                    .collect(Collectors.toList());

            log.info("Eklenecek yeni hisse senedi sayısı: {}", newStocks.size());

            // Mevcut hisseleri güncelle
            stockRepository.saveAll(existingStocks);
            log.info("{} mevcut hisse senedi fiyatı güncellendi", existingStocks.size());

            // Yeni hisseleri ekle (varsa)
            if (!newStocks.isEmpty()) {
                stockRepository.saveAll(newStocks);
                log.info("{} yeni hisse senedi veritabanına kaydedildi", newStocks.size());
            }

            // Tüm güncellenmiş ve yeni hisseleri döndür
            return stockRepository.findAll();
        } catch (Exception e) {
            log.error("Hisse senetleri API'den güncellenirken hata: {}", e.getMessage(), e);
            return stockRepository.findAll();
        }
    }

    /**
     * Her 30 dakikada bir hisse fiyatlarını günceller
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 dakikada bir
    public void scheduleStockPricesUpdate() {
        try {
            log.info("Zamanlanmış görev: Hisse senedi fiyatları güncelleniyor");

            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();

            if (prices.isEmpty()) {
                log.warn("API'den hisse senedi fiyatları alınamadı");
                return;
            }

            // Cache'i güncelle
            stockPriceCache.clear();
            prices.forEach(price -> stockPriceCache.put(price.getStockCode(), price.getPrice()));

            log.info("{} hisse senedi fiyatı güncellendi", prices.size());
        } catch (Exception e) {
            log.error("Zamanlanmış fiyat güncellemesi sırasında hata: {}", e.getMessage(), e);
        }
    }
}
