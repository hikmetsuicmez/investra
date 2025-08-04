package com.investra.dtos.response.infina;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockPriceResponse {

    @JsonProperty("Sonuc")
    private boolean success;

    @JsonProperty("Hata")
    private String error;

    @JsonProperty("FiyatListesi")
    private List<StockPrice> priceList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockPrice {

        @JsonProperty("HISSE_KODU")
        private String stockCode;

        @JsonProperty("FIYAT")
        private BigDecimal price;

        @JsonProperty("FIYAT_TARIH")
        private String priceDate;

        @JsonProperty("FIYAT_SAAT")
        private String priceTime;

        @JsonProperty("YUZDE_DEGISIM")
        private BigDecimal percentChange;

        @JsonProperty("FIYAT_TL")
        private BigDecimal priceTL;

        @JsonProperty("HACIM_LOT")
        private BigDecimal volumeLot;

        @JsonProperty("HACIM_TL")
        private BigDecimal volumeTL;
    }
}
