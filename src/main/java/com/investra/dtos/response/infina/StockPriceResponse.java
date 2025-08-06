package com.investra.dtos.response.infina;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockPriceResponse {

    @JsonProperty("result")
    private ResultData result;

    public boolean isSuccess() {
        return result != null && result.getData() != null && result.getData().getHisseFiyat() != null;
    }

    public String getError() {
        return null; // API yanıtında hata bilgisi yoksa null dönecek
    }

    public List<StockPrice> getPriceList() {
        return isSuccess() ? result.getData().getHisseFiyat() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultData {
        @JsonProperty("header")
        private Object header;

        @JsonProperty("data")
        private StockPriceData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockPriceData {
        @JsonProperty("HisseFiyat")
        private List<StockPrice> hisseFiyat;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockPrice {
        @JsonProperty("record_id")
        private String recordId;

        @JsonProperty("asset_code")
        private String stockCode; // API'den asset_code olarak geliyor, kodunuzda stockCode olarak kullanılıyor

        @JsonProperty("close_price")
        private BigDecimal price; // API'den close_price olarak geliyor, kodunuzda price olarak kullanılıyor

        @JsonProperty("record_date")
        private String recordDate;

        @JsonProperty("data_date")
        private String dataDate;

        @JsonProperty("high_price")
        private BigDecimal highPrice;

        @JsonProperty("low_price")
        private BigDecimal lowPrice;

        @JsonProperty("open_price")
        private BigDecimal openPrice;
    }
}
