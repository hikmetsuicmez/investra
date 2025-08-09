package com.investra.service;

import com.investra.config.InfinaApiConfig;
import com.investra.dtos.response.infina.StockDefinitionResponse;
import com.investra.dtos.response.infina.StockPriceResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InfinaApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InfinaApiConfig infinaApiConfig;

    @InjectMocks
    private InfinaApiService infinaApiService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllStockDefinitions_ShouldReturnList_WhenResponseIsValid() {
        when(infinaApiConfig.getStockDefinitionUrl()).thenReturn("http://mock-stock-definitions");

        StockDefinitionResponse.StockDefinition sampleStock = new StockDefinitionResponse.StockDefinition();
        sampleStock.setCode("ABC");
        sampleStock.setSecurityDesc("Sample Stock");

        StockDefinitionResponse.StockDefinitionData data = new StockDefinitionResponse.StockDefinitionData();
        data.setHisseTanim(java.util.Collections.singletonList(sampleStock));

        StockDefinitionResponse.ResultData resultData = new StockDefinitionResponse.ResultData();
        resultData.setData(data);

        StockDefinitionResponse mockResponse = new StockDefinitionResponse();
        mockResponse.setResult(resultData);

        when(restTemplate.getForObject(anyString(), eq(StockDefinitionResponse.class))).thenReturn(mockResponse);

        List<StockDefinitionResponse.StockDefinition> definitions = infinaApiService.getAllStockDefinitions();

        assertNotNull(definitions);
        assertEquals(1, definitions.size());
        assertEquals("ABC", definitions.get(0).getCode());
        assertEquals("Sample Stock", definitions.get(0).getSecurityDesc());
    }

    @Test
    public void getAllStockDefinitions_ShouldReturnEmptyList_WhenResponseIsNull() {
        when(infinaApiConfig.getStockDefinitionUrl()).thenReturn("http://mock-stock-definitions");
        when(restTemplate.getForObject(anyString(), eq(StockDefinitionResponse.class))).thenReturn(null);

        List<StockDefinitionResponse.StockDefinition> definitions = infinaApiService.getAllStockDefinitions();

        assertNotNull(definitions);
        assertTrue(definitions.isEmpty());
    }

    @Test
    public void getStockPrice_ShouldReturnEmptyOptional_WhenResponseIsInvalid() {
        when(infinaApiConfig.getStockPriceUrl()).thenReturn("http://mock-stock-price");
        when(restTemplate.getForObject(anyString(), eq(StockPriceResponse.class))).thenReturn(null);

        Optional<StockPriceResponse.StockPrice> result = infinaApiService.getStockPrice("XYZ");

        assertFalse(result.isPresent());
    }

    @Test
    public void getAllStockPrices_ShouldReturnEmptyList_WhenResponseIsInvalid() {
        when(infinaApiConfig.getStockPriceUrl()).thenReturn("http://mock-stock-price");
        when(restTemplate.getForObject(anyString(), eq(StockPriceResponse.class))).thenReturn(null);

        List<StockPriceResponse.StockPrice> prices = infinaApiService.getAllStockPrices();

        assertNotNull(prices);
        assertTrue(prices.isEmpty());
    }
}