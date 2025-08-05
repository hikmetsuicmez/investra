package com.investra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
public class InfinaApiConfig {

    @Value("${infina.api.base-url}")
    private String baseUrl;

    @Value("${infina.api.key}")
    private String apiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getStockDefinitionUrl() {
        return baseUrl + "/HisseTanim?&api_key=" + apiKey;
    }

    public String getStockPriceUrl() {
        return baseUrl + "/HisseFiyat?&api_key=" + apiKey;
    }
}
