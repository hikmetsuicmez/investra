package com.investra.service.helper;

import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.entity.Stock;
import com.investra.enums.ExecutionType;
import com.investra.exception.InactiveStockException;
import com.investra.exception.ValidationException;
import com.investra.repository.StockRepository;
import com.investra.service.helper.EntityFinderService.OrderEntities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderValidatorService {

    private final StockRepository stockRepository;

    private static final LocalTime MARKET_OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(23, 0);

    public void validateSellOrderRequest(StockSellOrderRequest request) {
        try {
            if (request == null) {
                log.warn("İstek nesnesi null.");
                throw new ValidationException("İstek boş olamaz");
            }

            if (request.getClientId() == null || request.getClientId() <= 0) {
                log.warn("Geçersiz müşteri ID: {}", request.getClientId());
                throw new ValidationException("Geçerli bir müşteri ID'si gereklidir");
            }

            if (request.getStockId() == null || request.getStockId() <= 0) {
                log.warn("Geçersiz hisse senedi ID: {}", request.getStockId());
                throw new ValidationException("Geçerli bir hisse senedi ID'si gereklidir");
            }

            if (request.getQuantity() <= 0) {
                log.warn("Geçersiz miktar: {}", request.getQuantity());
                throw new ValidationException("Miktar 0'dan büyük olmalıdır");
            }

            if (request.getExecutionType() == null) {
                log.warn("Execution type null. Emir türü belirtilmelidir");
                throw new ValidationException("Emir türü belirtilmelidir");
            }

            if (request.getExecutionType() == ExecutionType.LIMIT) {
                if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("Limit emrinde geçersiz fiyat: {}", request.getPrice());
                    throw new ValidationException("Limit emrinde fiyat 0'dan büyük olmalıdır");
                }
            }

            log.debug("İstek doğrulama başarılı: {}", request);
        } catch (ValidationException e) {
            log.warn("İstek doğrulama hatası: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("İstek doğrulanırken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new ValidationException("İstek doğrulanırken beklenmeyen bir hata oluştu", e);
        }
    }

    public void validateBuyOrder(OrderEntities entities, StockBuyOrderRequest request) {
        try {
            if (request == null) {
                log.warn("İstek doğrulama hatası: İstek null");
                throw new ValidationException("İstek boş olamaz");
            }

            if (request.getClientId() == null || request.getClientId() <= 0) {
                log.warn("İstek doğrulama hatası: Geçersiz müşteri ID: {}", request.getClientId());
                throw new ValidationException("Geçerli bir müşteri ID'si gereklidir");
            }

            if (request.getAccountId() == null || request.getAccountId() <= 0) {
                log.warn("İstek doğrulama hatası: Geçersiz hesap ID: {}", request.getAccountId());
                throw new ValidationException("Geçerli bir hesap ID'si gereklidir");
            }

            if (request.getStockId() == null || request.getStockId() <= 0) {
                log.warn("İstek doğrulama hatası: Geçersiz hisse senedi ID: {}", request.getStockId());
                throw new ValidationException("Geçerli bir hisse senedi ID'si gereklidir");
            }

            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                log.warn("İstek doğrulama hatası: Geçersiz miktar: {}", request.getQuantity());
                throw new ValidationException("Miktar 0'dan büyük olmalıdır");
            }

            if (request.getExecutionType() == null) {
                log.warn("Execution type null: Emir türü belirtilmelidir ");
                throw new ValidationException("Emir türü belirtilmelidir");
            }

            if (request.getExecutionType() == ExecutionType.LIMIT && (request.getPrice() == null || request.getPrice() <= 0)) {
                log.warn("İstek doğrulama hatası: Geçersiz limit fiyat: {}", request.getPrice());
                throw new ValidationException("Limit emrinde fiyat 0'dan büyük olmalıdır");
            }

            // Borsa açık mı kontrolü
            if (!isMarketOpen()) {
                throw new ValidationException("Borsa şu anda kapalı. İşlem saatleri: 10:00 - 18:00");
            }

            // Hisse senedi aktif mi kontrolü
            if (!entities.stock().getIsActive()) {
                throw new InactiveStockException("Hisse senedi aktif değil: " + entities.stock().getCode());
            }

            log.debug("Alış isteği doğrulama başarılı: {}", request);
        } catch (ValidationException | InactiveStockException e) {
            log.warn("Alış isteği doğrulama hatası: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Alış isteği doğrulanırken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new ValidationException("Alış isteği doğrulanırken beklenmeyen bir hata oluştu", e);
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
            throw new InactiveStockException("Hisse senedi aktif değil: " + stock.getCode());
        }
    }

    public boolean isMarketOpen() {
        LocalTime now = LocalDateTime.now().toLocalTime();
        return !now.isBefore(MARKET_OPEN_TIME) && !now.isAfter(MARKET_CLOSE_TIME);
    }
}
