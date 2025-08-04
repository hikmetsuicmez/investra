package com.investra.service;

import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.dtos.response.infina.StockPriceResponse;
import com.investra.entity.Stock;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * Tüm hisse senetlerini getirir, önce veritabanından
     * yoksa API'den çeker ve veritabanına kaydeder
     */
    @Cacheable("stocks")
    public List<Stock> getAllStocks() {
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
    @CacheEvict(value = "stocks", allEntries = true)
    public List<Stock> refreshStocksFromApi() {
        try {
            log.info("Hisse senedi tanımları API'den yenileniyor");

            // Hisse tanımlarını API'den al
            List<StockDefinitionResponse.StockDefinition> definitions = infinaApiService.getAllStockDefinitions();

            if (definitions.isEmpty()) {
                log.warn("API'den hisse senedi tanımı alınamadı");
                return stockRepository.findAll();
            }

            // Hisse fiyatlarını API'den al
            List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();

            // Fiyatları code bazında map'e dönüştür
            Map<String, BigDecimal> priceMap = prices.stream()
                    .collect(Collectors.toMap(
                            StockPriceResponse.StockPrice::getStockCode,
                            StockPriceResponse.StockPrice::getPrice,
                            (price1, price2) -> price1 // Duplicate durumunda ilkini al
                    ));

            // Cache'i güncelle
            stockPriceCache.clear();
            stockPriceCache.putAll(priceMap);

            // Hisseleri entity'ye dönüştür ve kaydet
            List<Stock> stocks = definitions.stream()
                    .filter(def -> "1".equals(def.getActive())) // Sadece aktif hisseleri al
                    .map(def -> {
                        Stock stock = stockRepository.findByCode(def.getStockCode())
                                .orElse(new Stock());

                        stock.setCode(def.getStockCode());
                        stock.setName(def.getStockName());
                        stock.setSector(def.getSectorName());
                        stock.setExchangeCode(def.getExchangeCode());

                        // Fiyatı varsa ekle
                        priceMap.computeIfPresent(def.getStockCode(), (code, price) -> {
                            stock.setPrice(price);
                            return price;
                        });

                        return stock;
                    })
                    .collect(Collectors.toList());

            stockRepository.saveAll(stocks);
            log.info("{} hisse senedi veritabanına kaydedildi", stocks.size());

            return stocks;

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

            log.info("{} hisse senedi fiyat�� güncellendi", prices.size());
        } catch (Exception e) {
            log.error("Zamanlanmış fiyat güncellemesi sırasında hata: {}", e.getMessage(), e);
        }
    }
}
