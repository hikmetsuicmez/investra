package com.investra.service.impl;

import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.dtos.response.infina.StockPriceResponse;
import com.investra.entity.Stock;
import com.investra.enums.StockGroup;
import com.investra.repository.StockRepository;
import com.investra.service.InfinaApiService;
import com.investra.service.StockService;
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
public class StockServiceImpl implements StockService {

    private final InfinaApiService infinaApiService;
    private final StockRepository stockRepository;

    // Kod-fiyat eşleşmesi için in-memory cache
    private final Map<String, BigDecimal> stockPriceCache = new ConcurrentHashMap<>();

    // üm hisse senetlerini getirir, önce veritabanından yoksa API'den çeker ve
    // veritabanına kaydeder
    @Cacheable(value = "stocks", key = "'all_stocks'")
    @Override
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
    @Override
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
    @Override
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
    @Override
    public List<Stock> refreshStocksFromApi() {
        try {
            log.info("Hisse senedi verileri API'den yenileniyor");

            // Önce hisse fiyatlarını al ve debug için logla
            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();
            log.info("HisseFiyat API'den {} adet fiyat alındı", prices != null ? prices.size() : 0);

            // Fiyatları hisse kodu bazında map'e al (.E uzantısını kaldırarak)
            Map<String, BigDecimal> priceMap = new HashMap<>();
            if (prices != null) {
                for (StockPriceResponse.StockPrice price : prices) {
                    String stockCode = price.getStockCode(); // Helper method .E uzantısını kaldırıyor
                    BigDecimal priceValue = price.getPrice(); // Helper method closePrice'ı dönüyor

                    if (stockCode != null && priceValue != null) {
                        priceMap.put(stockCode, priceValue);
                        log.debug("Fiyat eklendi: {} -> {}", stockCode, priceValue);
                    }
                }
            }
            log.info("Fiyat map'ine {} adet fiyat eklendi", priceMap.size());

            // Sonra hisse tanımlarını al
            List<StockDefinitionResponse.StockDefinition> definitions = infinaApiService.getAllStockDefinitions();
            log.info("HisseTanim API'den {} adet tanım alındı", definitions != null ? definitions.size() : 0);

            // Mevcut hisseleri al
            List<Stock> existingStocks = stockRepository.findAll();
            Map<String, Stock> existingStockMap = existingStocks.stream()
                    .collect(Collectors.toMap(Stock::getCode, stock -> stock, (existing, replacement) -> existing));

            List<Stock> stocksToSave = new ArrayList<>();

            // Her bir tanım için işlem yap
            if (definitions != null) {
                for (StockDefinitionResponse.StockDefinition def : definitions) {
                    if (def.getCode() == null)
                        continue;

                    String stockCode = def.getCode();
                    BigDecimal price = priceMap.get(stockCode);

                    // Sadece hem tanım hem de fiyat bilgisi olan hisseleri ekle
                    if (price != null) {
                        Stock stock = existingStockMap.getOrDefault(stockCode, new Stock());

                        // Tanım bilgilerini güncelle
                        stock.setCode(stockCode);
                        stock.setName(def.getSecurityDesc());
                        stock.setSector(def.getSector());
                        stock.setExchangeCode(def.getExchange());
                        stock.setIsin(def.getIsin());
                        stock.setMarket(def.getMarket());
                        stock.setSubMarket(def.getSubMarket());
                        stock.setCurrency(def.getCurrency());
                        stock.setSecurityDesc(def.getSecurityDesc());
                        stock.setIssuerName(def.getIssuerName());
                        stock.setIsActive("ACTIVE".equals(def.getStatus()));
                        stock.setGroup(StockGroup.FINANCE);
                        stock.setPrice(price);

                        stocksToSave.add(stock);
                        log.debug("Hisse eklendi/güncellendi: {} -> {} TL", stockCode, price);
                    } else {
                        log.debug("Hisse {} için fiyat bulunamadığından eklenmedi", stockCode);
                    }
                }
            }

            // Toplu kaydet ve log
            List<Stock> savedStocks = stockRepository.saveAll(stocksToSave);
            log.info("Toplam {} hisse kaydedildi", savedStocks.size());

            // Cache'i güncelle
            stockPriceCache.clear();
            priceMap.forEach(stockPriceCache::put);
            log.info("Cache güncellendi, {} adet fiyat cache'e alındı", stockPriceCache.size());

            return savedStocks;
        } catch (Exception e) {
            log.error("Hisse senetleri güncellenirken hata: {} - {}", e.getMessage(), e);
            return stockRepository.findAll();
        }
    }

    // Her 30 dakikada bir hisse fiyatlarını günceller
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 dakikada bir
    public void scheduleStockPricesUpdate() {
        try {
            log.info("Zamanlanmış görev: Hisse senedi fiyatları güncelleniyor");

            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();

            if (prices.isEmpty()) {
                log.warn("API'den hisse senedi fiyatları alınamadı");
                return;
            }

            Map<String, BigDecimal> newPrices = new HashMap<>();
            prices.forEach(price -> newPrices.put(price.getStockCode(), price.getPrice()));

            // Yeni veriler başarıyla alındıysa, cache'i güncelle
            if (!newPrices.isEmpty()) {
                stockPriceCache.clear();
                stockPriceCache.putAll(newPrices);
                log.info("Hisse fiyatları başarıyla güncellendi. Güncellenen hisse sayısı: {}", newPrices.size());
            }

            log.info("{} hisse senedi fiyatı güncellendi", prices.size());
        } catch (Exception e) {
            log.error("Zamanlanmış fiyat güncellemesi sırasında hata: {}", e.getMessage(), e);
        }
    }
}
