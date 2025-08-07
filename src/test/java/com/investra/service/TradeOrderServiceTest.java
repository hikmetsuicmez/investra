package com.investra.service;

import com.investra.entity.*;
import com.investra.enums.*;
import com.investra.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class TradeOrderServiceTest {

    @Mock
    private TradeOrderRepository tradeOrderRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PortfolioService portfolioService;

    @InjectMocks
    private TradeOrderService tradeOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testProcessWaitingOrder_Success() {
        // Arrange
        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setSubmittedAt(LocalDateTime.now().minusMinutes(1));
        order.setUser(new User());
        order.getUser().setEmail("user@example.com");

        when(tradeOrderRepository.save(any(TradeOrder.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tradeOrderService.processWaitingOrder(order);

        // Assert
        assertEquals(OrderStatus.EXECUTED, order.getStatus());
        assertNotNull(order.getExecutedAt());
        assertEquals(SettlementStatus.PENDING, order.getSettlementStatus());
        assertTrue(order.isFundsReserved());
        verify(tradeOrderRepository).save(order);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testProcessPendingLimitOrder_PriceConditionMet_BuyOrder() {
        // Arrange
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setPrice(BigDecimal.valueOf(100));

        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setOrderType(OrderType.BUY);
        order.setPrice(BigDecimal.valueOf(150));
        order.setStock(stock);
        order.setStatus(OrderStatus.PENDING);

        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(tradeOrderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tradeOrderService.processPendingLimitOrder(order);

        // Assert
        verify(tradeOrderRepository).save(order);
        assertEquals(OrderStatus.EXECUTED, order.getStatus());
    }
    @Test
    public void testProcessPendingLimitOrder_PriceConditionNotMet_SellOrder() {
        // Arrange
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setPrice(BigDecimal.valueOf(90)); // Piyasa fiyatı 90

        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setOrderType(OrderType.SELL);
        order.setPrice(BigDecimal.valueOf(100)); // Limit fiyatı 100 > piyasa fiyatı
        order.setStock(stock);
        order.setStatus(OrderStatus.PENDING);

        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        // Act
        tradeOrderService.processPendingLimitOrder(order);

        // Assert
        // Emir işlenmemeli çünkü fiyat şartı sağlanmadı
        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(tradeOrderRepository, never()).save(order); // save çağrılmamalı
    }

    @Test
    public void testCancelOrder_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("username");

        Account account = new Account();
        account.setId(1L);
        account.setAvailableBalance(BigDecimal.valueOf(1000));

        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderType(OrderType.BUY);
        order.setAccount(account);
        order.setNetAmount(BigDecimal.valueOf(100));
        order.setStock(new Stock());

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(tradeOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(tradeOrderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(notificationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        TradeOrder cancelledOrder = tradeOrderService.cancelOrder(1L, "username");

        // Assert
        assertEquals(OrderStatus.CANCELLED, cancelledOrder.getStatus());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(accountRepository, times(1)).save(account);
    }
    @Test
    public void testSettleCompletedOrder_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        Account account = new Account();
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setAvailableBalance(BigDecimal.valueOf(800));

        Stock stock = new Stock();
        stock.setId(1L);

        Client client = new Client();
        client.setId(1L);

        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setAccount(account);
        order.setStock(stock);
        order.setClient(client);
        order.setUser(user);
        order.setOrderType(OrderType.BUY);
        order.setSettlementStatus(SettlementStatus.PENDING);
        order.setStatus(OrderStatus.EXECUTED);
        order.setNetAmount(BigDecimal.valueOf(200));
        order.setQuantity(10);
        order.setPrice(BigDecimal.valueOf(20));

        when(tradeOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(tradeOrderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(accountRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tradeOrderService.settleCompletedOrder(order);

        // Assert
        assertEquals(SettlementStatus.COMPLETED, order.getSettlementStatus());
        assertFalse(order.isFundsReserved());
        assertNotNull(order.getSettledAt());
        verify(accountRepository).save(account);
        verify(tradeOrderRepository).save(order);
        verify(portfolioService).updatePortfolioWithEntities(
                eq(order.getId()),
                eq(order.getOrderType()),
                eq(order.getQuantity()),
                eq(order.getPrice()),
                eq(stock),
                eq(account),
                eq(client)
        );
        verify(notificationRepository).save(any(Notification.class));
    }
    @Test
    public void testGetAllOrdersByUser_Success() {
        User user = new User();
        user.setUsername("username");

        TradeOrder order1 = new TradeOrder();
        TradeOrder order2 = new TradeOrder();

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(tradeOrderRepository.findByUserOrderBySubmittedAtDesc(user))
                .thenReturn(List.of(order1, order2));

        List<TradeOrder> orders = tradeOrderService.getAllOrdersByUser("username");

        assertEquals(2, orders.size());
        verify(userRepository).findByUsername("username");
        verify(tradeOrderRepository).findByUserOrderBySubmittedAtDesc(user);
    }
    @Test
    public void testGetOrdersByStatusAndUser_Success() {
        User user = new User();
        user.setUsername("username");

        TradeOrder order1 = new TradeOrder();
        TradeOrder order2 = new TradeOrder();

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        when(tradeOrderRepository.findByUserAndStatusOrderBySubmittedAtDesc(user, OrderStatus.PENDING))
                .thenReturn(List.of(order1, order2));

        List<TradeOrder> orders = tradeOrderService.getOrdersByStatusAndUser("username", OrderStatus.PENDING);

        assertEquals(2, orders.size());
        verify(userRepository).findByUsername("username");
        verify(tradeOrderRepository).findByUserAndStatusOrderBySubmittedAtDesc(user, OrderStatus.PENDING);
    }
    @Test
    public void testUpdateAccountBalanceForBuyOrder() {
        Account account = new Account();
        account.setAvailableBalance(BigDecimal.valueOf(1000));

        BigDecimal amount = BigDecimal.valueOf(200);

        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        tradeOrderService.updateAccountBalanceForBuyOrder(account, amount);

        assertEquals(BigDecimal.valueOf(800), account.getAvailableBalance());
        verify(accountRepository).save(account);
    }
    @Test
    public void testRestoreAccountBalanceForCancelledBuyOrder() {
        Account account = new Account();
        account.setAvailableBalance(BigDecimal.valueOf(800));

        BigDecimal amount = BigDecimal.valueOf(200);

        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        tradeOrderService.restoreAccountBalanceForCancelledBuyOrder(account, amount);

        assertEquals(BigDecimal.valueOf(1000), account.getAvailableBalance());
        verify(accountRepository).save(account);
    }
    @Test
    public void testProcessPendingLimitOrder_StockNotFound() {
        TradeOrder order = new TradeOrder();
        order.setId(1L);
        order.setStock(new Stock());
        order.getStock().setId(999L); // Geçersiz ID
        order.setStatus(OrderStatus.PENDING);

        when(stockRepository.findById(999L)).thenReturn(Optional.empty());

        tradeOrderService.processPendingLimitOrder(order);

        assertEquals(OrderStatus.PENDING, order.getStatus());
        verify(tradeOrderRepository, never()).save(any());
    }

}