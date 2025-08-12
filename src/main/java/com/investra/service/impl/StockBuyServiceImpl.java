package com.investra.service.impl;

import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import com.investra.exception.*;
import com.investra.mapper.StockMapper;
import com.investra.repository.*;
import com.investra.service.AbstractStockTradeService;
import com.investra.service.SimulationDateService;
import com.investra.service.StockBuyService;
import com.investra.service.TradeOrderService;
import com.investra.service.helper.*;
import com.investra.service.helper.EntityFinderService.OrderEntities;
import com.investra.service.helper.OrderCalculationService.OrderCalculation;
import com.investra.service.helper.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StockBuyServiceImpl extends AbstractStockTradeService implements StockBuyService {

        private final StockRepository stockRepository;
        private final AccountRepository accountRepository;
        private final PortfolioItemRepository portfolioItemRepository;
        private final PortfolioRepository portfolioRepository;
        private final TradeOrderRepository tradeOrderRepository;

        // Yardımcı servisler
        private final OrderValidatorService validatorService;
        private final EntityFinderService entityFinderService;
        private final OrderCalculationService calculationService;
        private final PortfolioUpdateService portfolioUpdateService;
        private final OrderPreviewCacheService previewCacheService;
        private final TradeOrderService tradeOrderService;
        private final SimulationDateService simulationDateService;

        public StockBuyServiceImpl(
                        ClientRepository clientRepository,
                        StockRepository stockRepository,
                        AccountRepository accountRepository,
                        PortfolioItemRepository portfolioItemRepository,
                        PortfolioRepository portfolioRepository,
                        TradeOrderRepository tradeOrderRepository,
                        OrderValidatorService validatorService,
                        EntityFinderService entityFinderService,
                        OrderCalculationService calculationService,
                        PortfolioUpdateService portfolioUpdateService,
                        OrderPreviewCacheService previewCacheService,
                        TradeOrderService tradeOrderService,
                        SimulationDateService simulationDateService) {
                super(clientRepository);
                this.stockRepository = stockRepository;
                this.accountRepository = accountRepository;
                this.portfolioItemRepository = portfolioItemRepository;
                this.portfolioRepository = portfolioRepository;
                this.tradeOrderRepository = tradeOrderRepository;
                this.validatorService = validatorService;
                this.entityFinderService = entityFinderService;
                this.calculationService = calculationService;
                this.portfolioUpdateService = portfolioUpdateService;
                this.previewCacheService = previewCacheService;
                this.tradeOrderService = tradeOrderService;
                this.simulationDateService = simulationDateService;
        }

        @Override
        @Cacheable(value = "stocks", key = "'available'")
        public Response<List<StockResponse>> getAvailableStocks() {
                try {
                        List<Stock> stocks = stockRepository.findAll();
                        log.info("Aktif hisse senedi sayısı: {}", stocks.size());

                        return Response.<List<StockResponse>>builder()
                                        .statusCode(HttpStatus.OK.value())
                                        .data(stocks.stream().map(StockMapper::toStockResponse).toList())
                                        .build();
                } catch (Exception e) {
                        log.error("Mevcut hisse senetleri listelenirken hata oluştu: {}", e.getMessage());
                        return Response.<List<StockResponse>>builder()
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .message("Mevcut hisse senetleri listelenirken hata oluştu")
                                        .errorCode(ExceptionUtil.getErrorCode(e))
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
                                throw new InsufficientBalanceException();
                        }

                        // Önizleme yanıtı oluşturulur
                        StockBuyOrderPreviewResponse response = createBuyOrderPreviewResponse(entities, calculation);

                        // Önizleme önbelleğe alınır (execute için)
                        String previewId = UUID.randomUUID().toString();
                        previewCacheService.cacheOrderPreview(previewId, request);
                        response.setPreviewId(previewId);

                        return Response.<StockBuyOrderPreviewResponse>builder()
                                        .statusCode(HttpStatus.OK.value())
                                        .message("Hisse senedi alış önizlemesi başarıyla oluşturuldu")
                                        .data(response)
                                        .build();
                } catch (BaseException e) {
                        log.error("Error previewing buy order: {}", e.getMessage());
                        return Response.<StockBuyOrderPreviewResponse>builder()
                                        .statusCode(HttpStatus.BAD_REQUEST.value())
                                        .message(e.getMessage())
                                        .errorCode(ExceptionUtil.getErrorCode(e))
                                        .build();
                } catch (Exception e) {
                        log.error("Unexpected error previewing buy order: {}", e.getMessage(), e);
                        return Response.<StockBuyOrderPreviewResponse>builder()
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .message("Hisse senedi alış önizlemesi oluşturulurken beklenmeyen bir hata oluştu")
                                        .errorCode(ExceptionUtil.getErrorCode(e))
                                        .build();
                }
        }

        @Override
        @Transactional
        public Response<StockBuyOrderResultResponse> executeBuyOrder(StockBuyOrderRequest request, String userEmail) {
                try {
                        log.info(
                                        "Alış emri işlemi başlatıldı. Kullanıcı: {}, Müşteri ID: {}, Hesap ID: {}, Hisse ID: {}, Adet: {}, Emir Tipi: {}, Fiyat: {}",
                                        userEmail, request.getClientId(), request.getAccountId(), request.getStockId(),
                                        request.getQuantity(), request.getExecutionType(), request.getPrice());

                        validatorService.validateOrderExecution(request.getStockId());

                        // Preview ID kontrolü - sadece previewId'nin varlığını kontrol ediyoruz
                        if (request.getPreviewId() == null || request.getPreviewId().trim().isEmpty()) {
                                log.warn("Boş preview ID ile işlem denemesi yapıldı. Kullanıcı: {}", userEmail);
                                throw new ValidationException("Preview ID boş olamaz");
                        }

                        // Önizleme istediğinin önbellekte olup olmadığı kontrol edilir
                        Object cachedRequest = previewCacheService.getOrderPreview(request.getPreviewId());

                        if (cachedRequest == null || !(cachedRequest instanceof StockBuyOrderRequest)) {
                                log.warn("Önizleme ID geçersiz veya süresi dolmuş. ID: {}, Kullanıcı: {}",
                                                request.getPreviewId(),
                                                userEmail);
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

                        // Portfolio kontrolü - müşterinin portfolio'su olmalı
                        Portfolio portfolio = portfolioRepository.findByClient(entities.client())
                                        .orElseThrow(() -> new BusinessException(
                                                        "Müşteri için portfolio bulunamadı. Önce portfolio oluşturulmalı."));

                        // Hesap bakiyesi kontrol edilir
                        if (entities.account().getAvailableBalance().doubleValue() < calculation.netAmount()
                                        .doubleValue()) {
                                throw new InsufficientBalanceException();
                        }

                        // İşlemi yapan kullanıcıyı bulma
                        User currentUser = entityFinderService.findUserByEmail(userEmail);

                        // TradeOrder kaydı oluşturulur
                        TradeOrder tradeOrder = TradeOrder.builder()
                                        .client(entities.client())
                                        .account(entities.account())
                                        .stock(entities.stock())
                                        .orderType(OrderType.BUY)
                                        .quantity(request.getQuantity())
                                        .price(calculation.price())
                                        .totalAmount(calculation.totalAmount())
                                        .netAmount(calculation.netAmount())
                                        .status(OrderStatus.PENDING)
                                        .executionType(request.getExecutionType())
                                        .user(currentUser)
                                        .submittedAt(LocalDateTime.now())
                                        .orderNumber(calculationService.generateOrderNumber())
                                        .build();

                        // Simülasyon tarihi ile trade oluştur ve durum ata (haftasonu yok)
                        LocalDate currentSimulationDate = simulationDateService.getCurrentSimulationDate();
                        tradeOrder.setTradeDate(currentSimulationDate);
                        tradeOrder.assignRandomStatus(currentSimulationDate);

                        tradeOrder = tradeOrderRepository.save(tradeOrder);

                        // Eğer alış emri bekleyen veya gerçekleşen ise, available balance azaltılır
                        if (tradeOrder.getExecutionType() == ExecutionType.MARKET &&
                                        (tradeOrder.getStatus() == OrderStatus.PENDING
                                                        || tradeOrder.getStatus() == OrderStatus.EXECUTED)) {
                                tradeOrderService.updateAccountBalanceForBuyOrder(entities.account(),
                                                calculation.netAmount());
                        } else if (tradeOrder.getExecutionType() == ExecutionType.LIMIT &&
                                        tradeOrder.getStatus() == OrderStatus.EXECUTED) {
                                // Limit emirlerde sadece emir gerçekleştiğinde bakiyeyi güncelle
                                tradeOrderService.updateAccountBalanceForBuyOrder(entities.account(),
                                                calculation.netAmount());
                        }

                        // Portföy güncelleme SADECE T+2 settlement tamamlandığında yapılacak
                        // Burada portföy güncellemesi YOK - T+2 settlement'ta yapılacak

                        // İşlem sonucu yanıtı oluşturulur
                        StockBuyOrderResultResponse response = createBuyOrderResultResponse(tradeOrder, calculation);

                        // Önizleme önbellekten temizlenir
                        previewCacheService.removeOrderPreview(request.getPreviewId());
                        log.info("Alım işlemi başarıyla tamamlandı. OrderNo: {}, Status: {}",
                                        tradeOrder.getOrderNumber(),
                                        tradeOrder.getStatus());

                        return Response.<StockBuyOrderResultResponse>builder()
                                        .statusCode(HttpStatus.OK.value())
                                        .data(response)
                                        .message("Hisse senedi alım işlemi başarıyla gerçekleştirildi")
                                        .build();

                } catch (BaseException e) {
                        log.error("Error executing buy order: {}", e.getMessage());
                        return Response.<StockBuyOrderResultResponse>builder()
                                        .statusCode(HttpStatus.BAD_REQUEST.value())
                                        .message(e.getMessage())
                                        .errorCode(ExceptionUtil.getErrorCode(e))
                                        .build();
                } catch (Exception e) {
                        log.error("Unexpected error executing buy order: {}", e.getMessage(), e);
                        return Response.<StockBuyOrderResultResponse>builder()
                                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                        .message("Hisse senedi alış işlemi gerçekleştirilirken beklenmeyen bir hata oluştu")
                                        .errorCode(ExceptionUtil.getErrorCode(e))
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
                                .stockSymbol(entities.stock().getCode())
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
                                .stockSymbol(tradeOrder.getStock().getCode())
                                .price(calculation.price())
                                .quantity(calculation.quantity())
                                .tradeDate(calculation.tradeDate())
                                .valueDate(calculation.valueDate())
                                .totalAmount(calculation.totalAmount())
                                .stockGroup(
                                                tradeOrder.getStock().getGroup() != null
                                                                ? tradeOrder.getStock().getGroup().name()
                                                                : "UNKNOWN")
                                .commission(calculation.commission())
                                .bsmv(calculation.bsmv())
                                .totalTaxAndCommission(calculation.totalTaxAndCommission())
                                .netAmount(calculation.netAmount())
                                .executionType(calculation.executionType())
                                .status(tradeOrder.getStatus())
                                .message("Hisse senedi alım işlemi başarıyla gerçekleştirildi")
                                .build();
        }

        // Alış işlemi için TradeOrder kaydı oluşturur
        private TradeOrder createTradeOrder(
                        OrderEntities entities,
                        OrderCalculation calculation,
                        User currentUser,
                        StockBuyOrderRequest request) {

                TradeOrder tradeOrder = TradeOrder.builder()
                                .orderNumber(calculationService.generateOrderNumber())
                                .client(entities.client())
                                .account(entities.account())
                                .stock(entities.stock())
                                .submittedAt(LocalDateTime.now())
                                .orderType(OrderType.BUY)
                                .executionType(request.getExecutionType())
                                .price(calculation.price())
                                .quantity(calculation.quantity())
                                .totalAmount(calculation.totalAmount())
                                .netAmount(calculation.netAmount())
                                .status(OrderStatus.EXECUTED)
                                .user(currentUser)
                                .build();

                // Simülasyon tarihi ile trade date set et
                LocalDate currentSimulationDate = simulationDateService.getCurrentSimulationDate();
                tradeOrder.setTradeDate(currentSimulationDate);

                return tradeOrder;
        }

}
