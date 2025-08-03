package com.investra.service.helper;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.enums.OrderType;
import com.investra.exception.InactiveStockException;
import com.investra.exception.StockNotFoundException;
import com.investra.exception.ValidationException;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidatorService {

    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final EntityFinderService entityFinderService;
    private final OrderPreviewCacheService previewCacheService;

    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(18, 0);

    public void validatePurchaseOrderRequest(StockOrderRequest request) {
        validateOrderRequestInternal(request, "Alış işlemi istek doğrulama başarılı: {}" + OrderType.BUY);
    }

    public void validateSellOrderRequest(StockOrderRequest request) {
        validateOrderRequestInternal(request, "Satış işlemi istek doğrulama başarılı: {}" + OrderType.SELL);
        PortfolioItem portfolioItem = portfolioItemRepository.findByClientIdAndStockId(
                request.getClientId(), request.getStockId())
                .orElseThrow(() -> new StockNotFoundException("Müşterinin portföyünde hisse senedi bulunamadı"));
        entityFinderService.validatePortfolioQuantity(portfolioItem, request.getQuantity());
    }

    private void validateOrderRequestInternal(StockOrderRequest request, String successLogMessage) {
        try {
            validateOrderRequest(request);
            validateClientAndStockId(request);
            request.validatePrice();
            //validateOrderExecution(request.getStockId());

            log.debug(successLogMessage, request);
        } catch (ValidationException e) {
            log.warn("İstek doğrulama hatası: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("İstek doğrulanırken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new ValidationException("İstek doğrulanırken beklenmeyen bir hata oluştu", e);
        }
    }

    public void validateOrderRequest(StockOrderRequest request) {
            if (request == null) {
                throw new ValidationException("İstek boş olamaz");
            }
            if (request.getExecutionType() == null) {
                throw new ValidationException("Emir türü belirtilmelidir");
            }
            if (request.getQuantity() <= 0) {
                throw new ValidationException("Miktar 0'dan büyük olmalıdır");
            }
    }

    private void validateClientAndStockId(StockOrderRequest request) {
        if (request.getClientId() == null || request.getClientId() <= 0) {
            throw new ValidationException("Geçerli bir müşteri ID'si gereklidir");
        }

        if (request.getStockId() == null || request.getStockId() <= 0) {
            throw new ValidationException("Geçerli bir hisse senedi ID'si gereklidir");
        }
    }

    public void validateOrderExecution(Long stockId) {
        // Borsa açık mı kontrolü
        if (!isMarketOpen()) {
            throw new ValidationException("Borsa şu anda kapalı. İşlem saatleri: 10:00 - 18:00");
        }

        // Hisse senedi aktif mi kontrolü
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ValidationException("Geçersiz hisse senedi ID: " + stockId));

        if (!stock.getIsActive()) {
            throw new InactiveStockException("Hisse senedi aktif değil: " + stock.getSymbol());
        }
    }

    public void createAndSaveSellOrder(StockOrderRequest request, String userEmail) {
        // Önizleme doğrulaması
        previewCacheService.validatePreview(request.getPreviewId(), request);
        // İsteği doğrula
        validateSellOrderRequest(request);
    }

    private boolean isMarketOpen() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return !now.isBefore(MARKET_OPEN_TIME) && !now.isAfter(MARKET_CLOSE_TIME);
    }
}
