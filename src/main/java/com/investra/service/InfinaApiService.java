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
            log.debug("API URL: {}", url);

            StockDefinitionResponse response = restTemplate.getForObject(url, StockDefinitionResponse.class);

            if (response != null && response.getResult() != null &&
                response.getResult().getData() != null &&
                response.getResult().getData().getHisseTanim() != null) {

                List<StockDefinitionResponse.StockDefinition> definitions = response.getResult().getData().getHisseTanim();
                log.info("Toplam {} hisse senedi tanımı alındı", definitions.size());
                return definitions;
            } else {
                log.warn("Hisse senedi tanımları alınamadı");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Hisse senedi tanımları alınırken hata: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    public Optional<StockPriceResponse.StockPrice> getStockPrice(String stockCode) {
        try {
            log.info("Hisse senedi fiyatı Infina API'sinden alınıyor: {}", stockCode);
            String url = infinaApiConfig.getStockPriceUrl() + "&hisse_kodu=" + stockCode;

            StockPriceResponse response = restTemplate.getForObject(url, StockPriceResponse.class);

            if (response != null && response.getResult() != null &&
                response.getResult().getData() != null &&
                response.getResult().getData().getHisseFiyat() != null &&
                !response.getResult().getData().getHisseFiyat().isEmpty()) {

                log.info("Hisse senedi fiyatı başarıyla alındı: {}", stockCode);
                return Optional.of(response.getResult().getData().getHisseFiyat().get(0));
            } else {
                log.error("Hisse senedi fiyatı alınamadı: {}", stockCode);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Hisse senedi fiyatı alınırken hata: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }


    public List<StockPriceResponse.StockPrice> getAllStockPrices() {
        try {
            log.info("Tüm hisse senedi fiyatları Infina API'sinden alınıyor");
            String url = infinaApiConfig.getStockPriceUrl();

            StockPriceResponse response = restTemplate.getForObject(url, StockPriceResponse.class);

            if (response != null && response.getResult() != null &&
                response.getResult().getData() != null &&
                response.getResult().getData().getHisseFiyat() != null) {

                List<StockPriceResponse.StockPrice> prices = response.getResult().getData().getHisseFiyat();
                log.info("Toplam {} hisse senedi fiyatı alındı", prices.size());
                return prices;
            } else {
                log.error("Hisse senedi fiyatları alınamadı");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Hisse senedi fiyatları alınırken hata: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
