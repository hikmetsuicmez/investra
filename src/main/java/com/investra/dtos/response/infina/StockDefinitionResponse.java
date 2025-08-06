package com.investra.dtos.response.infina;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDefinitionResponse {

    @JsonProperty("result")
    private ResultData result;

    public boolean isSuccess() {
        return result != null && result.getData() != null && result.getData().getHisseTanim() != null;
    }

    public String getError() {
        return null; // API yanıtında hata bilgisi yoksa null dönecek
    }

    public List<StockDefinition> getStockList() {
        return isSuccess() ? result.getData().getHisseTanim() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultData {
        @JsonProperty("header")
        private Object header;

        @JsonProperty("data")
        private StockDefinitionData data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockDefinitionData {
        @JsonProperty("HisseTanim")
        private List<StockDefinition> hisseTanim;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockDefinition {
        // API'den gelen alanlar burada belirtilen adlarla eşleştirilecek
        @JsonProperty("asset_code")
        private String stockCode; // Muhtemelen asset_code veya benzer bir şey

        @JsonProperty("asset_name")
        private String stockName; // Muhtemelen asset_name veya benzer bir şey

        @JsonProperty("exchange_code")
        private String exchangeCode; // Muhtemelen exchange_code veya benzer bir şey

        @JsonProperty("sector_code")
        private String sectorCode; // Muhtemelen sector_code veya benzer bir şey

        @JsonProperty("sector_name")
        private String sectorName; // Muhtemelen sector_name veya benzer bir şey

        @JsonProperty("market_cap")
        private Double marketCap; // Muhtemelen market_cap veya benzer bir şey

        @JsonProperty("active")
        private String active; // Muhtemelen active veya benzer bir şey
    }
}
