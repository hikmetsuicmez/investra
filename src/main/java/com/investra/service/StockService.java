package com.investra.service;

import com.investra.dtos.response.infina.StockPriceResponse;
import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.entity.Stock;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
    public List<Stock> refreshStocksFromApi() {
        try {
            log.info("Hisse senedi verileri API'den yenileniyor");

            // Hisse fiyatlarını al
            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();
            log.info("API'den {} hisse fiyatı alındı", prices != null ? prices.size() : 0);

            if (prices == null || prices.isEmpty()) {
                log.warn("API'den hisse fiyatları alınamadı");
                return stockRepository.findAll();
            }

            // Mevcut hisseleri al
            List<Stock> existingStocks = stockRepository.findAll();
            Map<String, Stock> existingStockMap = existingStocks.stream()
                    .collect(Collectors.toMap(Stock::getCode, stock -> stock));

            List<Stock> stocksToSave = new ArrayList<>();

            // Her bir fiyat için işlem yap
            for (StockPriceResponse.StockPrice price : prices) {
                if (price.getStockCode() == null) continue;

                Stock stock = existingStockMap.getOrDefault(price.getStockCode(), new Stock());

                // Temel bilgileri güncelle
                stock.setCode(price.getStockCode());
                if (stock.getName() == null) {
                    stock.setName(price.getStockCode());
                }
                stock.setPrice(price.getPrice());
                stock.setIsActive(true);

                stocksToSave.add(stock);
            }

            // Toplu kaydet
            List<Stock> savedStocks = stockRepository.saveAll(stocksToSave);
            log.info("{} hisse senedi güncellendi/eklendi", savedStocks.size());

            // Cache'i güncelle
            stockPriceCache.clear();
            prices.forEach(price -> {
                if (price.getStockCode() != null && price.getPrice() != null) {
                    stockPriceCache.put(price.getStockCode(), price.getPrice());
                }
            });

            return savedStocks;
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

            // Yeni bir geçici map oluştur
            Map<String, BigDecimal> newPrices = new HashMap<>();
            prices.forEach(price -> newPrices.put(price.getStockCode(), price.getPrice()));

            // Yeni veriler başarıyla alındıysa, cache'i güncelle
            if (!newPrices.isEmpty()) {
                stockPriceCache.clear();
                stockPriceCache.putAll(newPrices);
                log.info("Hisse fiyatları başarıyla güncellendi. Güncellenen hisse sayısı: {}", newPrices.size());
            }
        } catch (Exception e) {
            log.error("Zamanlanmış fiyat güncellemesi sırasında hata: {}", e.getMessage(), e);
        }
    }
}
