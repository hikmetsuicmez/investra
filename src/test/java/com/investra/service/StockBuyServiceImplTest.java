package com.investra.service;

import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockBuyOrderPreviewResponse;
import com.investra.dtos.response.StockBuyOrderResultResponse;
import com.investra.entity.*;
import com.investra.enums.*;
import com.investra.repository.*;
import com.investra.service.helper.*;
import com.investra.service.helper.EntityFinderService.OrderEntities;
import com.investra.service.helper.OrderCalculationService.OrderCalculation;
import com.investra.service.impl.StockBuyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StockBuyServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private StockRepository stockRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private PortfolioRepository portfolioRepository;
    @Mock private TradeOrderRepository tradeOrderRepository;

    @Mock private OrderValidatorService validatorService;
    @Mock private EntityFinderService entityFinderService;
    @Mock private OrderCalculationService calculationService;
    @Mock private PortfolioUpdateService portfolioUpdateService;
    @Mock private OrderPreviewCacheService previewCacheService;
    @Mock private TradeOrderService tradeOrderService;
    @Mock private SimulationDateService simulationDateService;

    @InjectMocks
    private StockBuyServiceImpl stockBuyService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        stockBuyService = new StockBuyServiceImpl(
                clientRepository,
                stockRepository,
                accountRepository,
                portfolioItemRepository,
                portfolioRepository,
                tradeOrderRepository,
                validatorService,
                entityFinderService,
                calculationService,
                portfolioUpdateService,
                previewCacheService,
                tradeOrderService,
                simulationDateService
        );
    }
    @Test
    public void testExecuteBuyOrder_InsufficientBalance() {
        String userEmail = "user@example.com";
        StockBuyOrderRequest request = new StockBuyOrderRequest();
        request.setClientId(1L);
        request.setAccountId(2L);
        request.setStockId(3L);
        request.setQuantity(10);
        request.setExecutionType(ExecutionType.MARKET);
        request.setPreviewId(UUID.randomUUID().toString());
        request.setPrice(100.0);

        Client client = new Client();
        client.setId(1L);

        Account account = new Account();
        account.setId(2L);
        account.setBalance(BigDecimal.valueOf(500)); // yetersiz bakiye
        account.setAvailableBalance(BigDecimal.valueOf(500));

        Stock stock = new Stock();
        stock.setId(3L);

        PortfolioItem portfolioItem = new PortfolioItem();

        OrderEntities entities = new OrderEntities(client, stock, portfolioItem, account);

        OrderCalculation calculation = new OrderCalculation(
                BigDecimal.valueOf(100), 10, LocalDate.now(), "T+2",
                BigDecimal.valueOf(5), BigDecimal.valueOf(1),
                BigDecimal.valueOf(6), BigDecimal.valueOf(1000),
                BigDecimal.valueOf(990), ExecutionType.MARKET
        );

        when(previewCacheService.getOrderPreview(request.getPreviewId())).thenReturn(request);
        doNothing().when(validatorService).validateOrderExecution(request.getStockId());
        when(entityFinderService.findOrderEntities(request.getClientId(), request.getAccountId(), request.getStockId()))
                .thenReturn(entities);
        doNothing().when(validatorService).validateBuyOrder(entities, request);
        when(calculationService.calculateOrderAmounts(client, stock, request.getQuantity(),
                request.getExecutionType(), request.getPrice(), OrderType.BUY))
                .thenReturn(calculation);

        Response<StockBuyOrderResultResponse> response = stockBuyService.executeBuyOrder(request, userEmail);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertNull(response.getData());

        verify(previewCacheService).getOrderPreview(request.getPreviewId());
        verify(validatorService).validateOrderExecution(request.getStockId());
        verify(entityFinderService).findOrderEntities(request.getClientId(), request.getAccountId(), request.getStockId());
        verify(validatorService).validateBuyOrder(entities, request);
        verify(calculationService).calculateOrderAmounts(client, stock, request.getQuantity(), request.getExecutionType(), request.getPrice(), OrderType.BUY);
        verifyNoInteractions(tradeOrderService);
        verify(previewCacheService, never()).removeOrderPreview(anyString());
    }
    @Test
    public void testPreviewBuyOrder_success() {
        StockBuyOrderRequest request = new StockBuyOrderRequest();
        request.setClientId(1L);
        request.setAccountId(2L);
        request.setStockId(3L);
        request.setQuantity(10);
        request.setExecutionType(ExecutionType.MARKET);
        request.setPrice(50.0);

        Client client = new Client();
        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(1000));

        Stock stock = new Stock();
        OrderEntities entities = new OrderEntities(client, stock, new PortfolioItem(),account );
        LocalDate tradeDate= LocalDate.now();
        String valueDate="t+2";
        BigDecimal commission= BigDecimal.valueOf(0.001);
        BigDecimal bsmv= BigDecimal.valueOf(0.05);
        BigDecimal totalTaxAndCommission = commission.add(bsmv);

        OrderCalculation calculation = new OrderCalculation(
                BigDecimal.valueOf(500),
                10,
                tradeDate,
                valueDate,
                commission,   // commission
                bsmv,   // bsmv
                totalTaxAndCommission,   // totalTaxAndComission
                BigDecimal.valueOf(500), // totalAmount
                BigDecimal.valueOf(1000),
                ExecutionType.MARKET
        );

        // Mocking validator ve diğer servis davranışları
        doNothing().when(validatorService).validateOrderExecution(3L);
        when(entityFinderService.findOrderEntities(1L, 2L, 3L)).thenReturn(entities);
        doNothing().when(validatorService).validateBuyOrder(entities, request);
        when(calculationService.calculateOrderAmounts(client, stock, 10, ExecutionType.MARKET, 50.0, OrderType.BUY))
                .thenReturn(calculation);

        // previewId mock
        doAnswer(invocation -> {
            String previewId = invocation.getArgument(0);
            return null;
        }).when(previewCacheService).cacheOrderPreview(anyString(), eq(request));

        Response<StockBuyOrderPreviewResponse> response = stockBuyService.previewBuyOrder(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("Hisse senedi alış önizlemesi başarıyla oluşturuldu", response.getMessage());
        assertNotNull(response.getData());
        assertNotNull(response.getData().getPreviewId());
        assertEquals(request.getExecutionType(), response.getData().getExecutionType());
    }
}