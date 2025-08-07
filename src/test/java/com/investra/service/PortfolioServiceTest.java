package com.investra.service;

import com.investra.entity.*;
import com.investra.enums.OrderType;
import com.investra.exception.AccountNotFoundException;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.StockNotFoundException;
import com.investra.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PortfolioServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdatePortfolioAfterSettlement_BuyOrder_NewPortfolioAndNewItem() {
        Client client = Client.builder().id(1L).build();
        Account account = Account.builder().id(10L).build();
        Stock stock = Stock.builder().id(100L).code("ISCTR").build();

        TradeOrder order = TradeOrder.builder()
                .id(999L)
                .client(client)
                .account(account)
                .stock(stock)
                .orderType(OrderType.BUY)
                .quantity(50)
                .price(BigDecimal.valueOf(20))
                .build();

        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(portfolioRepository.findByClient(client)).thenReturn(Optional.empty());

        Portfolio savedPortfolio = Portfolio.builder().id(500L).client(client).createdAt(LocalDateTime.now()).build();
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedPortfolio);
        when(portfolioItemRepository.findByClientIdAndStockId(client.getId(), stock.getId()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> portfolioService.updatePortfolioAfterSettlement(order));

        verify(portfolioRepository).save(any(Portfolio.class));
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
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

    @Test
    public void testUpdatePortfolioAfterSettlement_ExistingPortfolioAndItem_BuyOrder() {
        Long clientId = 1L;
        Long accountId = 2L;
        Long stockId = 3L;

        Client client = Client.builder().id(clientId).build();
        Account account = Account.builder().id(accountId).build();
        Stock stock = Stock.builder().id(stockId).code("GARAN").build();
        Portfolio portfolio = Portfolio.builder().id(99L).client(client).build();

        PortfolioItem existingItem = PortfolioItem.builder()
                .id(88L)
                .quantity(100)
                .avgPrice(BigDecimal.valueOf(10))
                .stock(stock)
                .account(account)
                .portfolio(portfolio)
                .build();

        TradeOrder order = TradeOrder.builder()
                .client(client)
                .account(account)
                .stock(stock)
                .orderType(OrderType.BUY)
                .quantity(50)
                .price(BigDecimal.valueOf(20))
                .build();

        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(portfolioRepository.findByClient(client)).thenReturn(Optional.of(portfolio));
        when(portfolioItemRepository.findByClientIdAndStockId(clientId, stockId))
                .thenReturn(Optional.of(existingItem));

        portfolioService.updatePortfolioAfterSettlement(order);

        verify(portfolioItemRepository).save(any(PortfolioItem.class));
    }
}