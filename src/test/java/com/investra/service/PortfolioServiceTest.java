package com.investra.service;

import com.investra.entity.*;
import com.investra.exception.StockNotFoundException;
import com.investra.repository.*;
import com.investra.service.impl.PortfolioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdatePortfolioAfterSettlement_StockNotFound() {
        TradeOrder order = TradeOrder.builder()
                .stock(Stock.builder().id(1L).build())
                .account(Account.builder().id(2L).build())
                .client(Client.builder().id(3L).build())
                .build();

        when(stockRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () ->
                portfolioService.updatePortfolioAfterSettlement(order));
    }

}