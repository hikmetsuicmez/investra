package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.*;
import com.investra.mapper.ClientMapper;
import com.investra.mapper.StockMapper;
import com.investra.repository.*;
import com.investra.service.helper.*;
import com.investra.service.helper.record.OrderCalculation;
import com.investra.service.helper.record.OrderEntities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockSellServiceImpl implements StockSellService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;

    // Yardımcı servisler
    private final OrderValidatorService validatorService;
    private final EntityFinderService entityFinderService;
    private final OrderCalculationService calculationService;
    private final PortfolioUpdateService portfolioUpdateService;
    private final OrderPreviewCacheService previewCacheService;

    @Override
    public Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request) {
        var strategy = getStringOptionalFunction(request);
        List<Client> clients = strategy != null
                ? strategy.apply(request.getSearchTerm()).map(List::of).orElse(List.of())
                : List.of();

        List<ClientSearchResponse> responseClients = clients.stream()
                .map(ClientMapper::mapToClientSearchResponse)
                .toList();

        return Response.<List<ClientSearchResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri bulundu")
                .data(responseClients)
                .build();
    }

    private Function<String, Optional<Client>> getStringOptionalFunction(ClientSearchRequest request) {
        var searchStrategies = Map.of(
                "TCKN", clientRepository::findByNationalityNumber,
                "VERGI_N0", clientRepository::findByTaxId,
                "MAVI_KART_NO", clientRepository::findByBlueCardNo,
                "NAME", (Function<String, Optional<Client>>) term ->
                        clientRepository.findAll().stream()
                                .filter(client -> client.getFullName().toLowerCase().contains(term.toLowerCase()))
                                .findFirst()

        );
        return searchStrategies.get(request.getSearchType());
    }

    @Override
    public Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());

        List<ClientStockHoldingResponse> stockHoldings = portfolioItems.stream()
                .map(StockMapper::mapToClientStockHoldingResponse)
                .toList();

        return Response.<List<ClientStockHoldingResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri bulundu")
                .data(stockHoldings)
                .build();
    }

    @Override
    @Transactional
    public Response<StockOrderPreviewResponse> previewSellOrder(StockOrderRequest request) {
        try {
            // İsteği doğrula
            validatorService.validateSellOrderRequest(request);

            // Gerekli varlıkları bul ve doğrula
            OrderEntities entities = entityFinderService.findAndValidateEntities(request);

            // Hesaplamaları yap
            OrderCalculation calculation = calculationService.calculateOrderAmounts(
                    entities.client(), entities.stock(), request);

            // Yanıt nesnesini oluştur
            StockOrderPreviewResponse previewResponse = calculationService.createPreviewResponse(
                    entities.account(), entities.stock(), request, calculation);

            // Önizlemeyi önbelleğe kaydet ve previewId döndür
            String previewId = previewCacheService.cachePreview(request, previewResponse);
            previewResponse.setPreviewId(previewId);

            return Response.<StockOrderPreviewResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Satış önizleme başarılı")
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
    @Transactional
    public Response<StockSellOrderResultResponse> executeSellOrder(StockOrderRequest request, String userEmail) {
        try {
            validatorService.createAndSaveSellOrder(request, userEmail);
            List<Object> responses =  entityFinderService.processOrder(request, userEmail);
            OrderEntities entities = (OrderEntities) responses.get(0);
            OrderCalculation calculation = (OrderCalculation) responses.get(1);
            TradeOrder tradeOrder = (TradeOrder) responses.get(2);
            User submittedBy = (User) responses.get(3);

            log.info("Kullanıcı: {} tarafından satış işlemi başlatıldı.", submittedBy.getEmail());

            // Portföy ve bakiye güncelleme işlemleri
            PortfolioItem updatedPortfolioItem = portfolioUpdateService.updatePortfolioAfterSell(
                    entities.portfolioItem(), request.getQuantity());

            // Portföy öğesi null ise (tüm hisseler satıldıysa) log kaydı düş
            if (updatedPortfolioItem == null) {
                log.info("Müşteri {} tüm {} hisselerini sattı, portföyden kaldırıldı.",
                        entities.client().getId(), entities.stock().getSymbol());
            }
            portfolioUpdateService.updateAccountBalanceAfterSell(entities.account(), calculation.netAmount());

            // Önizlemeyi önbellekten kaldır
            previewCacheService.removePreview(request.getPreviewId());

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
