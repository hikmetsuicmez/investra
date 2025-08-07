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
        return null;
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
        @JsonProperty("hisse_kodu")
        private String stockCode;

        @JsonProperty("fiyat")
        private BigDecimal price;

        @JsonProperty("tarih")
        private String date;

        @JsonProperty("saat")
        private String time;
    }
}
