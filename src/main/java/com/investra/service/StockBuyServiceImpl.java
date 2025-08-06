package com.investra.service;

import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.*;
import com.investra.mapper.StockMapper;
import com.investra.repository.*;
import com.investra.service.helper.*;
import com.investra.service.helper.EntityFinderService.OrderEntities;
import com.investra.service.helper.OrderCalculationService.OrderCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StockBuyServiceImpl extends AbstractStockTradeService implements StockBuyService {

    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;

    // Yardımcı servisler
    private final OrderValidatorService validatorService;
    private final EntityFinderService entityFinderService;
    private final OrderCalculationService calculationService;
    private final PortfolioUpdateService portfolioUpdateService;
    private final OrderPreviewCacheService previewCacheService;

    public StockBuyServiceImpl(
            ClientRepository clientRepository,
            StockRepository stockRepository,
            AccountRepository accountRepository,
            PortfolioItemRepository portfolioItemRepository,
            TradeOrderRepository tradeOrderRepository,
            OrderValidatorService validatorService,
            EntityFinderService entityFinderService,
            OrderCalculationService calculationService,
            PortfolioUpdateService portfolioUpdateService,
            OrderPreviewCacheService previewCacheService) {
        super(clientRepository);
        this.stockRepository = stockRepository;
        this.accountRepository = accountRepository;
        this.portfolioItemRepository = portfolioItemRepository;
        this.tradeOrderRepository = tradeOrderRepository;
        this.validatorService = validatorService;
        this.entityFinderService = entityFinderService;
        this.calculationService = calculationService;
        this.portfolioUpdateService = portfolioUpdateService;
        this.previewCacheService = previewCacheService;
    }

    @Override
    public Response<List<StockResponse>> getAvailableStocks() {
        try {
            List<Stock> stocks = stockRepository.findByIsActiveTrue();
            return Response.<List<StockResponse>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .data(stocks.stream().map(StockMapper::toStockResponse).toList())
                    .build();
        } catch (Exception e) {
            log.error("Mevcut hisse senetleri listelenirken hata oluştu: {}", e.getMessage());
            return Response.<List<StockResponse>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .isSuccess(false)
                    .message("Mevcut hisse senetleri listelenirken hata oluştu")
                    .errorCode(ErrorCode.UNEXPECTED_ERROR)
                    .build();
        }
    }

    @Override
    public Response<StockBuyOrderPreviewResponse> previewBuyOrder(StockBuyOrderRequest request) {
        try {
            // Borsa açık mı ve hisse senedi aktif mi kontrolü
            validatorService.validateOrderExecution(request.getStockId());

            // İşlem için gerekli varlıkları (müşteri, hesap, hisse) bulma
            OrderEntities entities = entityFinderService.findOrderEntities(
                    request.getClientId(),
                    request.getAccountId(),
                    request.getStockId());

            // İşlem istediğini doğrulama
            validatorService.validateBuyOrder(entities, request);

            // Hesap bakiyesi kontrol edilir
            OrderCalculation calculation = calculationService.calculateOrderAmounts(
                    entities.client(),
                    entities.stock(),
                    request.getQuantity(),
                    request.getExecutionType(),
                    request.getPrice(),
                    OrderType.BUY);

            if (entities.account().getBalance().doubleValue() < calculation.netAmount().doubleValue()) {
                throw new InsufficientBalanceException(String.valueOf(ErrorCode.INSUFFICIENT_BALANCE));
            }

            // Önizleme yanıtı oluşturulur
            StockBuyOrderPreviewResponse response = createBuyOrderPreviewResponse(entities, calculation);

            // Önizleme önbelleğe alınır (execute için)
            String previewId = UUID.randomUUID().toString();
            previewCacheService.cacheOrderPreview(previewId, request);
            response.setPreviewId(previewId);

            return Response.<StockBuyOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .message("Hisse senedi alış önizlemesi başarıyla oluşturuldu")
                    .data(response)
                    .build();
        } catch (BaseException e) {
            log.error("Error previewing buy order: {}", e.getMessage());
            return Response.<StockBuyOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .isSuccess(false)
                    .message(e.getMessage())
                    .errorCode(e.getErrorCode())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error previewing buy order: {}", e.getMessage(), e);
            return Response.<StockBuyOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .isSuccess(false)
                    .message("Hisse senedi alış önizlemesi oluşturulurken beklenmeyen bir hata oluştu")
                    .errorCode(ErrorCode.UNEXPECTED_ERROR)
                    .build();
        }
    }

    @Override
    @Transactional
    public Response<StockBuyOrderResultResponse> executeBuyOrder(StockBuyOrderRequest request, String userEmail) {
        try {
            validatorService.validateOrderExecution(request.getStockId());

            // Preview ID kontrolü - sadece previewId'nin varlığını kontrol ediyoruz
            if (request.getPreviewId() == null || request.getPreviewId().trim().isEmpty()) {
                throw new ValidationException("Preview ID boş olamaz");
            }

            // Önizleme istediğinin önbellekte olup olmadığı kontrol edilir
            Object cachedRequest = previewCacheService.getOrderPreview(request.getPreviewId());

            if (cachedRequest == null || !(cachedRequest instanceof StockBuyOrderRequest)) {
                throw new ValidationException("Geçersiz veya süresi dolmuş önizleme ID'si");
            }

            // İşlem için gerekli varlıkları (müşteri, hesap, hisse) bulma
            OrderEntities entities = entityFinderService.findOrderEntities(
                    request.getClientId(),
                    request.getAccountId(),
                    request.getStockId());

            // İşlem doğrulaması
            validatorService.validateBuyOrder(entities, request);

            // İşlem hesaplamaları yapılır
            OrderCalculation calculation = calculationService.calculateOrderAmounts(
                    entities.client(),
                    entities.stock(),
                    request.getQuantity(),
                    request.getExecutionType(),
                    request.getPrice(),
                    OrderType.BUY);

            // Hesap bakiyesi kontrol edilir
            if (entities.account().getBalance().doubleValue() < calculation.netAmount().doubleValue()) {
                throw new InsufficientBalanceException("Yetersiz bakiye: " + ErrorCode.INSUFFICIENT_BALANCE);
            }

            // İşlemi yapan kullanıcıyı bulma
            User currentUser = entityFinderService.findUserByEmail(userEmail);

            // TradeOrder kaydı oluşturulur
            TradeOrder tradeOrder = createTradeOrder(entities, calculation, currentUser, request);
            tradeOrder = tradeOrderRepository.save(tradeOrder);

            // Portföy güncellenir
            portfolioUpdateService.updatePortfolioForBuy(
                    entities.client(),
                    entities.account(),
                    entities.stock(),
                    request.getQuantity(),
                    calculation.netAmount());

            // İşlem sonucu yanıtı oluşturulur
            StockBuyOrderResultResponse response = createBuyOrderResultResponse(tradeOrder, calculation);

            // Önizleme önbellekten temizlenir
            previewCacheService.removeOrderPreview(request.getPreviewId());

            return Response.<StockBuyOrderResultResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .data(response)
                    .message("Hisse senedi alım işlemi başarıyla gerçekleştirildi")
                    .build();

        } catch (BaseException e) {
            log.error("Error executing buy order: {}", e.getMessage());
            return Response.<StockBuyOrderResultResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .isSuccess(false)
                    .message(e.getMessage())
                    .errorCode(e.getErrorCode())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error executing buy order: {}", e.getMessage(), e);
            return Response.<StockBuyOrderResultResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .isSuccess(false)
                    .message("Hisse senedi alış işlemi gerçekleştirilirken beklenmeyen bir hata oluştu")
                    .errorCode(ErrorCode.UNEXPECTED_ERROR)
                    .build();
        }
    }

    // Alış önizleme yanıtı oluşturur
    private StockBuyOrderPreviewResponse createBuyOrderPreviewResponse(
            OrderEntities entities, OrderCalculation calculation) {

        return StockBuyOrderPreviewResponse.builder()
                .accountNumber(entities.account().getAccountNumber())
                .operation("Alış")
                .stockName(entities.stock().getName())
                .stockSymbol(entities.stock().getSymbol())
                .price(calculation.price())
                .quantity(calculation.quantity())
                .tradeDate(calculation.tradeDate())
                .valueDate(calculation.valueDate())
                .totalAmount(calculation.totalAmount())
                .stockGroup(String.valueOf(entities.stock().getGroup()))
                .commission(calculation.commission())
                .bsmv(calculation.bsmv())
                .totalTaxAndCommission(calculation.totalTaxAndCommission())
                .netAmount(calculation.netAmount())
                .executionType(calculation.executionType())
                .build();
    }

    // Alış işlemi sonucunu oluşturur
    private StockBuyOrderResultResponse createBuyOrderResultResponse(
            TradeOrder tradeOrder, OrderCalculation calculation) {

        return StockBuyOrderResultResponse.builder()
                .orderId(tradeOrder.getId())
                .accountNumber(tradeOrder.getAccount().getAccountNumber())
                .operation("Alış")
                .stockName(tradeOrder.getStock().getName())
                .stockSymbol(tradeOrder.getStock().getSymbol())
                .price(calculation.price())
                .quantity(calculation.quantity())
                .tradeDate(calculation.tradeDate())
                .valueDate(calculation.valueDate())
                .totalAmount(calculation.totalAmount())
                .stockGroup(tradeOrder.getStock().getGroup() != null ? tradeOrder.getStock().getGroup().name() : "UNKNOWN")
                .commission(calculation.commission())
                .bsmv(calculation.bsmv())
                .totalTaxAndCommission(calculation.totalTaxAndCommission())
                .netAmount(calculation.netAmount())
                .executionType(calculation.executionType())
                .status(tradeOrder.getStatus())
                .executionTime(tradeOrder.getExecutedAt())
                .message("Hisse senedi alım işlemi başarıyla gerçekleştirildi")
                .build();
    }

    // Alış işlemi için TradeOrder kaydı oluşturur
    private TradeOrder createTradeOrder(
            OrderEntities entities,
            OrderCalculation calculation,
            User currentUser,
            StockBuyOrderRequest request) {

        return TradeOrder.builder()
                .orderNumber(calculationService.generateOrderNumber())
                .client(entities.client())
                .account(entities.account())
                .stock(entities.stock())
                .submittedAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now().plusDays(2))
                .orderType(OrderType.BUY)
                .executionType(request.getExecutionType())
                .price(calculation.price())
                .quantity(calculation.quantity())
                .totalAmount(calculation.totalAmount())
                .netAmount(calculation.netAmount())
                .status(OrderStatus.EXECUTED)
                .user(currentUser)
                .build();
    }


}
