package com.investra.service;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.ClientStockHoldingResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockSellOrderPreviewResponse;
import com.investra.dtos.response.StockSellOrderResultResponse;
import com.investra.entity.*;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.ValidationException;
import com.investra.repository.ClientRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.service.helper.*;

import com.investra.service.impl.StockSellServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StockSellServiceImplTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PortfolioItemRepository portfolioItemRepository;
    @Mock private TradeOrderRepository tradeOrderRepository;
    @Mock private OrderValidatorService validatorService;
    @Mock private EntityFinderService entityFinderService;
    @Mock private OrderCalculationService calculationService;
    @Mock private PortfolioUpdateService portfolioUpdateService;
    @Mock private OrderPreviewCacheService previewCacheService;
    @Mock private TradeOrderService tradeOrderService;

    @InjectMocks
    private StockSellServiceImpl stockSellService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        stockSellService = new StockSellServiceImpl(
                clientRepository,
                portfolioItemRepository,
                tradeOrderRepository,
                validatorService,
                entityFinderService,
                calculationService,
                portfolioUpdateService,
                previewCacheService,
                tradeOrderService
        );
    }

    @Test
    public void testGetClientStockHoldings_Success() {
        Long clientId = 1L;
        Client client = new Client();
        client.setId(clientId);

        Stock stock = new Stock();
        stock.setId(10L);
        stock.setCode("THYAO");

        PortfolioItem item = new PortfolioItem();
        item.setStock(stock);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(portfolioItemRepository.findByClientId(clientId)).thenReturn(List.of(item));

        Response<List<ClientStockHoldingResponse>> response = stockSellService.getClientStockHoldings(clientId);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(response.getData());
        assertFalse(response.getData().isEmpty());
        assertEquals(Long.valueOf(10L), response.getData().get(0).getStockId());
        verify(clientRepository).findById(clientId);
        verify(portfolioItemRepository).findByClientId(clientId);
    }

    @Test
    public void testGetClientStockHoldings_ClientNotFound() {
        Long clientId = 99L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Response<List<ClientStockHoldingResponse>> response = stockSellService.getClientStockHoldings(clientId);

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
        assertNull(response.getData());
        verify(clientRepository).findById(clientId);
    }
    @Test
    public void testPreviewSellOrder_Success() {
        StockSellOrderRequest request = new StockSellOrderRequest();
        request.setStockId(10L);
        request.setQuantity(5);
        request.setPreviewId("8");
        request.setPrice(BigDecimal.valueOf(100));
        request.setAccountId(8L);
        request.setClientId(9L);
        request.setExecutionType(ExecutionType.MARKET);

        // Mock: Doğrulama metotları void, hata atmaması yeterli
        doNothing().when(validatorService).validateSellOrderRequest(request);
        doNothing().when(validatorService).validateOrderExecution(request.getStockId());

        // Mock: EntityFinderService döndürsün
        Client client = new Client();
        client.setId(1L);
        Stock stock = new Stock();
        stock.setId(10L);
        Account account = new Account();
        PortfolioItem portfolioItem = new PortfolioItem();

        EntityFinderService.OrderEntities entities = new EntityFinderService.OrderEntities(
                client,
                stock,
                portfolioItem,
                account
        );

        when(entityFinderService.findAndValidateEntities(request)).thenReturn(entities);

        OrderCalculationService.OrderCalculation calculation = new OrderCalculationService.OrderCalculation(
                BigDecimal.valueOf(100),    // price
                0,                          // quantity
                null,                       // tradeDate
                null,                       // valueDate
                null,                       // commission
                null,                       // bsmv
                null,                       // totalTaxAndCommission
                BigDecimal.valueOf(500),    // totalAmount
                BigDecimal.valueOf(490),    // netAmount
                ExecutionType.MARKET        // executionType
        );

        when(calculationService.calculateOrderAmounts(client, stock, request)).thenReturn(calculation);

        StockSellOrderPreviewResponse previewResponse = StockSellOrderPreviewResponse.builder()
                .price(calculation.price())
                .totalAmount(calculation.totalAmount())
                .netAmount(calculation.netAmount())
                .build();

        when(calculationService.createPreviewResponse(account, stock, request, calculation)).thenReturn(previewResponse);

        // Mock: Önizleme cache'e kaydetme
        when(previewCacheService.cachePreview(request, previewResponse)).thenReturn("preview-123");

        Response<StockSellOrderPreviewResponse> response = stockSellService.previewSellOrder(request);

        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(response.getData());
        assertEquals("preview-123", response.getData().getPreviewId());
        verify(validatorService).validateSellOrderRequest(request);
        verify(validatorService).validateOrderExecution(request.getStockId());
        verify(entityFinderService).findAndValidateEntities(request);
        verify(calculationService).calculateOrderAmounts(client, stock, request);
        verify(calculationService).createPreviewResponse(account, stock, request, calculation);
        verify(previewCacheService).cachePreview(request, previewResponse);
    }

    @Test
    public void testPreviewSellOrder_ValidationException() {
        StockSellOrderRequest request = new StockSellOrderRequest();

        doThrow(new ValidationException("Geçersiz istek"))
                .when(validatorService).validateSellOrderRequest(request);

        Response<StockSellOrderPreviewResponse> response = stockSellService.previewSellOrder(request);

        assertFalse(response.isSuccess());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode());
        assertEquals("Geçersiz istek", response.getMessage());
        assertNull(response.getData());
        verify(validatorService).validateSellOrderRequest(request);
    }
    @Test
    public void testExecuteSellOrder_Success() {
        String userEmail = "user@example.com";
        String previewId = "preview123";

        StockSellOrderRequest request = new StockSellOrderRequest();
        request.setPreviewId(previewId);
        request.setStockId(1L);
        request.setQuantity(5);
        request.setExecutionType(ExecutionType.MARKET);

        StockSellOrderRequest cachedRequest = request; // Önizleme cache'den aynı isteği döndürecek

        User user = new User();
        user.setEmail(userEmail);

        Client client = new Client();
        client.setId(1L);

        Stock stock = new Stock();
        stock.setId(1L);
        stock.setCode("THYAO");

        Account account = new Account();
        PortfolioItem portfolioItem = new PortfolioItem();

        EntityFinderService.OrderEntities entities = new EntityFinderService.OrderEntities(client, stock, portfolioItem, account);

        OrderCalculationService.OrderCalculation calculation = new OrderCalculationService.OrderCalculation(
                BigDecimal.valueOf(100),
                request.getQuantity(),
                null, null, null, null, null,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(490),
                request.getExecutionType()
        );

        TradeOrder tradeOrder = TradeOrder.builder()
                .id(100L)
                .client(client)
                .account(account)
                .stock(stock)
                .orderType(OrderType.SELL)
                .quantity(request.getQuantity())
                .price(calculation.price())
                .totalAmount(calculation.totalAmount())
                .netAmount(calculation.netAmount())
                .status(OrderStatus.PENDING)
                .executionType(request.getExecutionType())
                .user(user)
                .submittedAt(LocalDateTime.now())
                .orderNumber("ORD123")
                .build();

        // Mocks
        when(previewCacheService.getPreviewRequest(previewId)).thenReturn(cachedRequest);
        doNothing().when(validatorService).validateSellOrderRequest(request);
        when(entityFinderService.findUserByEmail(userEmail)).thenReturn(user);
        when(entityFinderService.findAndValidateEntities(request)).thenReturn(entities);
        when(calculationService.calculateOrderAmounts(client, stock, request)).thenReturn(calculation);
        when(calculationService.generateOrderNumber()).thenReturn("ORD123");

        tradeOrder.setStatus(OrderStatus.EXECUTED);

        when(tradeOrderRepository.save(any(TradeOrder.class))).thenReturn(tradeOrder);
        when(portfolioUpdateService.updatePortfolioAfterSell(portfolioItem, request.getQuantity())).thenReturn(null);
        doNothing().when(previewCacheService).removeOrderPreview(previewId);

        Response<StockSellOrderResultResponse> response = stockSellService.executeSellOrder(request, userEmail);

        System.out.println("Response: " + response);
        System.out.println("isSuccess: " + response.isSuccess());
        System.out.println("StatusCode: " + response.getStatusCode());
        System.out.println("Message: " + response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertNotNull(response.getData());
        assertEquals(tradeOrder.getId(), response.getData().getOrderId());
        assertEquals(OrderStatus.EXECUTED, response.getData().getOrderStatus());
        verify(previewCacheService).getPreviewRequest(previewId);
        verify(validatorService).validateSellOrderRequest(request);
        verify(entityFinderService).findUserByEmail(userEmail);
        verify(entityFinderService).findAndValidateEntities(request);
        verify(calculationService).calculateOrderAmounts(client, stock, request);
        verify(tradeOrderRepository).save(any(TradeOrder.class));
        verify(portfolioUpdateService).updatePortfolioAfterSell(portfolioItem, request.getQuantity());
        verify(previewCacheService).removeOrderPreview(previewId);
    }
}