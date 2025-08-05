package com.investra.dtos.response.infina;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDefinitionResponse {

    @JsonProperty("Sonuc")
    private boolean success;

    @JsonProperty("Hata")
    private String error;

    @JsonProperty("HisseListesi")
    private List<StockDefinition> stockList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockDefinition {

        @JsonProperty("HISSE_KODU")
        private String stockCode;

        @JsonProperty("HISSE_ADI")
        private String stockName;

        @JsonProperty("BORSA_KODU")
        private String exchangeCode;

        @JsonProperty("SEKTOR_KODU")
        private String sectorCode;

        @JsonProperty("SEKTOR_ADI")
        private String sectorName;

        @JsonProperty("PD")
        private Double marketCap;

        @JsonProperty("AKTIF")
        private String active;
    }
}
