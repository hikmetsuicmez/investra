package com.investra.dtos.response.infina;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class StockDefinitionResponse {
    private StockDefinitionData data;

    @Data
    public static class StockDefinitionData {
        @JsonProperty("HisseTanim")
        private List<StockDefinition> stockDefinitions;
    }

    @Data
    public static class StockDefinition {
        private String country;
        @JsonProperty("short_sale_policy")
        private String shortSalePolicy;
        private String code;
        private Integer precision;
        @JsonProperty("gross_settl")
        private String grossSettl;
        @JsonProperty("market_desc")
        private String marketDesc;
        @JsonProperty("sub_market_desc")
        private String subMarketDesc;
        @JsonProperty("maximum_lot")
        private Integer maximumLot;
        private String type;
        private String issuer;
        @JsonProperty("security_desc")
        private String securityDesc;
        private String security;
        private String currency;
        @JsonProperty("lot_size")
        private Integer lotSize;
        private String sector;
        @JsonProperty("security_type")
        private String securityType;
        @JsonProperty("legacy_code")
        private String legacyCode;
        @JsonProperty("market_sector")
        private String marketSector;
        private String ticker;
        @JsonProperty("short_sell_enabled")
        private Boolean shortSellEnabled;
        private String index;
        @JsonProperty("max_order_value")
        private Integer maxOrderValue;
        @JsonProperty("issuer_name")
        private String issuerName;
        @JsonProperty("sub_market")
        private String subMarket;
        private String market;
        @JsonProperty("ptt_row")
        private String pttRow;
        @JsonProperty("last_update_time")
        private String lastUpdateTime;
        private String unit;
        private String domain;
        private String exchange;
        @JsonProperty("listing_date")
        private String listingDate;
        @JsonProperty("minimum_lot")
        private Integer minimumLot;
        private String isin;
        private String status;
    }
}
