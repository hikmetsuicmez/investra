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

        @JsonProperty("close_price")
        private BigDecimal closePrice;

        @JsonProperty("record_date")
        private String recordDate;

        @JsonProperty("asset_code")
        private String assetCode;

        @JsonProperty("high_price")
        private BigDecimal highPrice;

        @JsonProperty("data_date")
        private String dataDate;

        @JsonProperty("low_price")
        private BigDecimal lowPrice;

        @JsonProperty("open_price")
        private BigDecimal openPrice;

        // Helper methods
        public String getStockCode() {
            if (assetCode == null) return null;
            return assetCode.replace(".E", "");
        }

        public BigDecimal getPrice() {
            return this.closePrice;
        }
    }
}
