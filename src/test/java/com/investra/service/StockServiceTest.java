package com.investra.service;
import com.investra.entity.Stock;
import com.investra.repository.StockRepository;
import com.investra.service.impl.StockServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StockServiceTest {

    @Mock
    private InfinaApiService infinaApiService;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllStocks_FromRepository() {
        List<Stock> expectedStocks = List.of(new Stock(), new Stock());
        when(stockRepository.findAll()).thenReturn(expectedStocks);

        List<Stock> result = stockService.getAllStocks();

        assertEquals(2, result.size());
        verify(stockRepository, times(1)).findAll();
        verify(infinaApiService, never()).getAllStockDefinitions();
    }
    @Test
    public void testGetAllStocks_ReturnsEmptyList_WhenRepositoryReturnsEmpty() {
        when(stockRepository.findAll()).thenReturn(List.of());

        List<Stock> stocks = stockService.getAllStocks();

        assertNotNull(stocks);
        assertTrue(stocks.isEmpty());
    }

    @Test
    public void testGetStockByCode_FoundInRepository() {
        Stock stock = new Stock();
        stock.setCode("ISCTR");

        when(stockRepository.findByCode("ISCTR")).thenReturn(Optional.of(stock));
        when(infinaApiService.getStockPrice("ISCTR")).thenReturn(Optional.empty());

        Optional<Stock> result = stockService.getStockByCode("ISCTR");

        assertTrue(result.isPresent());
        assertEquals("ISCTR", result.get().getCode());
    }

    @Test
    public void testGetStockByCode_NotFound_ThenFoundAfterRefresh() {
        when(stockRepository.findByCode("THYAO"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new Stock()));

        when(infinaApiService.getAllStockDefinitions()).thenReturn(Collections.emptyList());
        when(infinaApiService.getAllStockPrices()).thenReturn(Collections.emptyList());

        Optional<Stock> result = stockService.getStockByCode("THYAO");

        assertTrue(result.isPresent());
    }

    @Test
    public void testRefreshStocksFromApi_ErrorFallbackToRepo() {
        when(infinaApiService.getAllStockDefinitions()).thenThrow(new RuntimeException("API error"));
        when(stockRepository.findAll()).thenReturn(List.of(new Stock()));

        List<Stock> result = stockService.refreshStocksFromApi();

        assertEquals(1, result.size());
        verify(stockRepository, times(1)).findAll();
    }

}