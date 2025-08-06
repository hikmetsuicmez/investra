package com.investra.service;

import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.dtos.response.infina.StockPriceResponse;
import com.investra.config.InfinaApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfinaApiService {

    private final RestTemplate restTemplate;
    private final InfinaApiConfig infinaApiConfig;


    public List<StockDefinitionResponse.StockDefinition> getAllStockDefinitions() {
        try {
            log.info("Hisse senedi tanımları Infina API'sinden alınıyor");
            String url = infinaApiConfig.getStockDefinitionUrl();

            log.info("API URL: {}", url); // URL'yi loglamak için eklendi

            StockDefinitionResponse response = restTemplate.getForObject(url, StockDefinitionResponse.class);

            if (response != null) {
                log.info("API yanıtı: success={}, error={}", response.isSuccess(), response.getError());
            } else {
                log.error("API'den boş yanıt alındı");
            }

            if (response != null && response.isSuccess()) {
                log.info("Toplam {} hisse senedi tanımı alındı",
                        response.getStockList() != null ? response.getStockList().size() : 0);
                return response.getStockList();
            } else {
                log.error("Hisse senedi tanımları alınırken hata: {}",
                        response != null ? response.getError() : "Yanıt alınamadı");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Hisse senedi tanımları alınırken beklenmeyen hata: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    public Optional<StockPriceResponse.StockPrice> getStockPrice(String stockCode) {
        try {
            log.info("Hisse senedi fiyatı Infina API'sinden alınıyor: {}", stockCode);
            String url = infinaApiConfig.getStockPriceUrl() + "&hisse_kodu=" + stockCode;

            StockPriceResponse response = restTemplate.getForObject(url, StockPriceResponse.class);

            if (response != null && response.isSuccess() && response.getPriceList() != null && !response.getPriceList().isEmpty()) {
                log.info("Hisse senedi fiyatı başarıyla alındı: {}", stockCode);
                return Optional.of(response.getPriceList().get(0));
            } else {
                log.error("Hisse senedi fiyatı alınırken hata: {}",
                        response != null ? response.getError() : "Yanıt alınamadı");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Hisse senedi fiyatı alınırken beklenmeyen hata: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }


    public List<StockPriceResponse.StockPrice> getAllStockPrices() {
        try {
            log.info("Tüm hisse senedi fiyatları Infina API'sinden alınıyor");
            String url = infinaApiConfig.getStockPriceUrl();

            StockPriceResponse response = restTemplate.getForObject(url, StockPriceResponse.class);

            if (response != null && response.isSuccess()) {
                log.info("Toplam {} hisse senedi fiyatı alındı",
                        response.getPriceList() != null ? response.getPriceList().size() : 0);
                return response.getPriceList();
            } else {
                log.error("Hisse senedi fiyatları alınırken hata: {}",
                        response != null ? response.getError() : "Yanıt alınamadı");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Hisse senedi fiyatları alınırken beklenmeyen hata: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
