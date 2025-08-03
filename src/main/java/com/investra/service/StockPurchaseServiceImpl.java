package com.investra.service;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.dtos.response.StockPurchaseOrderResultResponse;
import com.investra.dtos.response.StockSellOrderResultResponse;
import com.investra.entity.PortfolioItem;
import com.investra.entity.TradeOrder;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.*;
import com.investra.repository.TradeOrderRepository;
import com.investra.service.helper.*;
import com.investra.service.helper.record.OrderCalculation;
import com.investra.service.helper.record.OrderEntities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPurchaseServiceImpl implements StockPurchaseService {

    private final OrderValidatorService validatorService;
    private final EntityFinderService entityFinderService;
    private final OrderCalculationService calculationService;
    private final OrderPreviewCacheService previewCacheService;
    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioUpdateService portfolioUpdateService;

    @Override
    public Response<StockOrderPreviewResponse> previewPurchaseOrder(StockOrderRequest request) {
    try {
        validatorService.validatePurchaseOrderRequest(request);
        OrderEntities entities = entityFinderService.findAndValidateEntities(request);
        OrderCalculation calculation = calculationService.calculateOrderAmounts(
                entities.client(), entities.stock(), request);

        StockOrderPreviewResponse previewResponse = calculationService.createPreviewResponse(
                entities.account(),
                entities.stock(),
                request,
                calculation
        );
        previewResponse.setOrderType(OrderType.BUY);

        String previewId = previewCacheService.cachePreview(request,previewResponse);
        previewResponse.setPreviewId(previewId);

        return Response.<StockOrderPreviewResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Alış önizleme başarılı")
                .data(previewResponse)
                .build();
    } catch (ValidationException | StockNotFoundException | ClientNotFoundException |
             AccountNotFoundException | InsufficientStockException | InactiveStockException e) {
        log.warn("Önizleme hatası: {}", e.getMessage());
        return Response.<StockOrderPreviewResponse>builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    } catch (Exception e) {
        log.error("Önizleme sırasında beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
        return Response.<StockOrderPreviewResponse>builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Önizleme sırasında bir hata oluştu")
                .build();
    }
    }
    @Override
    public Response<StockPurchaseOrderResultResponse> executePurchaseOrder(StockOrderRequest request, String userEmail) {
        try {
            validatorService.createAndSaveSellOrder(request, userEmail);
            List<Object> responses =  entityFinderService.processOrder(request, userEmail);
            OrderEntities entities = (OrderEntities) responses.get(0);
            OrderCalculation calculation = (OrderCalculation) responses.get(1);
            TradeOrder tradeOrder = (TradeOrder) responses.get(2);
            User submittedBy = (User) responses.get(3);
            log.info("Kullanıcı: {} tarafından satış işlemi başlatıldı.", submittedBy.getEmail());

            PortfolioItem updatedPortfolioItem = portfolioUpdateService.updatePortfolioAfterPurchase(
                    entities.portfolioItem(), request.getQuantity());

            portfolioUpdateService.updateAccountBalanceAfterPurchase(
                    entities.account(), calculation.netAmount());

            previewCacheService.removePreview(request.getPreviewId());

            log.info("Alış emri oluşturuldu: {}", tradeOrder.getId());

            StockPurchaseOrderResultResponse stockPurchaseOrderResultResponse = StockPurchaseOrderResultResponse.builder()
                    .orderId(tradeOrder.getId())
                    .orderStatus(tradeOrder.getStatus())
                    .message("Alış emri başarıyla oluşturuldu")
                    .submittedAt(tradeOrder.getSubmittedAt())
                    .success(true)
                    .build();

            return Response.<StockPurchaseOrderResultResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Alış emri başarıyla oluşturuldu")
                    .data(stockPurchaseOrderResultResponse)
                    .build();

        } catch (ValidationException | StockNotFoundException | ClientNotFoundException |
                 AccountNotFoundException | InsufficientStockException | InactiveStockException e) {
            log.warn("Alış işlemi hatası: {}", e.getMessage());
            StockPurchaseOrderResultResponse errorResponse = StockPurchaseOrderResultResponse.builder()
                    .message(e.getMessage())
                    .success(false)
                    .build();

            return Response.<StockPurchaseOrderResultResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .data(errorResponse)
                    .build();
        } catch (Exception e) {
            log.error("Alış işlemi sırasında beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
            StockPurchaseOrderResultResponse errorResponse = StockPurchaseOrderResultResponse.builder()
                    .message("Alış işlemi sırasında bir hata oluştu")
                    .success(false)
                    .build();

            return Response.<StockPurchaseOrderResultResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Alış işlemi sırasında bir hata oluştu")
                    .data(errorResponse)
                    .build();
        }
    }
}
