package com.investra.service;

import com.investra.dtos.request.StockSellOrderRequest;
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

@Service
@Slf4j
public class StockSellServiceImpl extends AbstractStockTradeService implements StockSellService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;

    // Yardımcı servisler
    private final OrderValidatorService validatorService;
    private final EntityFinderService entityFinderService;
    private final OrderCalculationService calculationService;
    private final PortfolioUpdateService portfolioUpdateService;
    private final OrderPreviewCacheService previewCacheService;

    public StockSellServiceImpl(
            ClientRepository clientRepository,
            PortfolioItemRepository portfolioItemRepository,
            TradeOrderRepository tradeOrderRepository,
            OrderValidatorService validatorService,
            EntityFinderService entityFinderService,
            OrderCalculationService calculationService,
            PortfolioUpdateService portfolioUpdateService,
            OrderPreviewCacheService previewCacheService) {
        super(clientRepository);
        this.portfolioItemRepository = portfolioItemRepository;
        this.tradeOrderRepository = tradeOrderRepository;
        this.validatorService = validatorService;
        this.entityFinderService = entityFinderService;
        this.calculationService = calculationService;
        this.portfolioUpdateService = portfolioUpdateService;
        this.previewCacheService = previewCacheService;
    }

    @Override
    public Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId) {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ClientNotFoundException(clientId));

            List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());

            List<ClientStockHoldingResponse> stockHoldings = portfolioItems.stream()
                    .map(StockMapper::mapToClientStockHoldingResponse)
                    .toList();

            return Response.<List<ClientStockHoldingResponse>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .message("Müşteri portföy bilgileri getirildi")
                    .data(stockHoldings)
                    .build();
        } catch (ClientNotFoundException e) {
            log.warn("Müşteri bulunamadı: {}", e.getMessage());
            return Response.<List<ClientStockHoldingResponse>>builder()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .isSuccess(false)
                    .message(e.getMessage())
                    .errorCode(ErrorCode.CLIENT_NOT_FOUND)
                    .build();
        } catch (Exception e) {
            log.error("Müşteri portföy bilgileri getirilirken hata oluştu: {}", e.getMessage(), e);
            return Response.<List<ClientStockHoldingResponse>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .isSuccess(false)
                    .message("Portföy bilgileri getirilirken hata oluştu")
                    .errorCode(ErrorCode.UNEXPECTED_ERROR)
                    .build();
        }
    }

    @Override
    @Transactional
    public Response<StockSellOrderPreviewResponse> previewSellOrder(StockSellOrderRequest request) {
        try {
            // İsteği doğrula
            validatorService.validateSellOrderRequest(request);

            // Borsa ve hisse senedi durumunu kontrol et
            validatorService.validateOrderExecution(request.getStockId());

            // Gerekli varlıkları bul ve doğrula
            OrderEntities entities = entityFinderService.findAndValidateEntities(request);

            // Hesaplamaları yap
            OrderCalculation calculation = calculationService.calculateOrderAmounts(
                    entities.client(), entities.stock(), request);

            // Yanıt nesnesini oluştur
            StockSellOrderPreviewResponse previewResponse = calculationService.createPreviewResponse(
                    entities.account(), entities.stock(), request, calculation);

            // Önizlemeyi önbelleğe kaydet ve previewId döndür
            String previewId = previewCacheService.cachePreview(request, previewResponse);
            previewResponse.setPreviewId(previewId);

            return Response.<StockSellOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .message("Satış önizleme başarılı")
                    .data(previewResponse)
                    .build();
        } catch (BaseException e) {
            log.warn("Önizleme hatası: {}", e.getMessage());
            return Response.<StockSellOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .isSuccess(false)
                    .message(e.getMessage())
                    .errorCode(e.getErrorCode())
                    .build();
        } catch (Exception e) {
            log.error("Önizleme sırasında beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
            return Response.<StockSellOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .isSuccess(false)
                    .message("Önizleme sırasında bir hata oluştu")
                    .errorCode(ErrorCode.UNEXPECTED_ERROR)
                    .build();
        }
    }

    @Override
    @Transactional
    public Response<StockSellOrderResultResponse> executeSellOrder(StockSellOrderRequest request, String userEmail) {
        try {
            // Preview onayı kontrolü
            if (request.getPreviewId() == null) {
                throw new ValidationException("Preview ID gereklidir");
            }

            // Önizleme istediğinin önbellekte olup olmadığı kontrol edilir
            StockSellOrderRequest cachedRequest = previewCacheService.getPreviewRequest(request.getPreviewId());

            if (cachedRequest == null) {
                throw new ValidationException("Geçersiz veya süresi dolmuş önizleme ID'si");
            }

            // İsteği doğrula
            validatorService.validateSellOrderRequest(request);

            // Borsa ve hisse senedi durumunu kontrol et
            // validatorService.validateOrderExecution(request.getStockId());

            // Kullanıcıyı bul
            User submittedBy = entityFinderService.findUserByEmail(userEmail);
            log.info("Kullanıcı: {} tarafından satış işlemi başlatıldı.", submittedBy.getEmail());

            // Gerekli varlıkları bul ve doğrula
            OrderEntities entities = entityFinderService.findAndValidateEntities(request);

            // Hesaplamaları yap
            OrderCalculation calculation = calculationService.calculateOrderAmounts(
                    entities.client(), entities.stock(), request);

            // Satış emrini oluştur ve kaydet
            TradeOrder tradeOrder = TradeOrder.builder()
                    .client(entities.client())
                    .account(entities.account())
                    .stock(entities.stock())
                    .orderType(OrderType.SELL)
                    .quantity(request.getQuantity())
                    .price(calculation.price())
                    .totalAmount(calculation.totalAmount())
                    .status(OrderStatus.EXECUTED)
                    .executionType(request.getExecutionType())
                    .user(submittedBy)
                    .submittedAt(LocalDateTime.now())
                    .executedAt(LocalDateTime.now().plusDays(2))
                    .netAmount(calculation.netAmount())
                    .orderNumber(calculationService.generateOrderNumber())
                    .build();

            tradeOrder = tradeOrderRepository.save(tradeOrder);

            // Portföy ve bakiye güncelleme işlemleri
            PortfolioItem updatedPortfolioItem = portfolioUpdateService.updatePortfolioAfterSell(
                    entities.portfolioItem(), request.getQuantity());

            // Portföy öğesi null ise (tüm hisseler satıldıysa) log kaydı düş
            if (updatedPortfolioItem == null) {
                log.info("Müşteri {} tüm {} hisselerini sattı, portföyden kaldırıldı.",
                        entities.client().getId(), entities.stock().getSymbol());
            }
            portfolioUpdateService.updateAccountBalanceAfterSell(entities.account(), calculation.netAmount());

            // Önizlemeyi önbellekten kald��r
            previewCacheService.removeOrderPreview(request.getPreviewId());

            log.info("Satış emri oluşturuldu: {}", tradeOrder.getId());

            // Yanıt oluştur
            StockSellOrderResultResponse response = StockSellOrderResultResponse.builder()
                    .orderId(tradeOrder.getId())
                    .message("Satış emri başarıyla oluşturuldu")
                    .orderStatus(tradeOrder.getStatus())
                    .submittedAt(tradeOrder.getSubmittedAt())
                    .success(true)
                    .build();

            return Response.<StockSellOrderResultResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Satış emri başarıyla oluşturuldu")
                    .data(response)
                    .build();
        } catch (ValidationException | StockNotFoundException | ClientNotFoundException |
                 AccountNotFoundException | InsufficientStockException | InactiveStockException e) {
            log.warn("Satış işlemi hatası: {}", e.getMessage());
            StockSellOrderResultResponse errorResponse = StockSellOrderResultResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();

            return Response.<StockSellOrderResultResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .isSuccess(true)
                    .message(e.getMessage())
                    .data(errorResponse)
                    .build();
        } catch (Exception e) {
            log.error("Satış işlemi sırasında beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
            StockSellOrderResultResponse errorResponse = StockSellOrderResultResponse.builder()
                    .message("Satış işlemi sırasında bir hata oluştu")
                    .success(false)
                    .build();

            return Response.<StockSellOrderResultResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Satış işlemi sırasında bir hata oluştu")
                    .data(errorResponse)
                    .build();
        }
    }
}
