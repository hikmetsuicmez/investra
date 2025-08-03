package com.investra.service.helper;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.dtos.response.StockPurchaseOrderPreviewResponse;
import com.investra.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class OrderPreviewCacheService {

    // Önizleme önbelleği: Anahtar = Önizleme ID, Değer = Önizleme detayları
    private final Map<String, PreviewCacheItem> previewCache = new ConcurrentHashMap<>();

    // Önizleme kaydının geçerlilik süresi (dakika)
    private static final int PREVIEW_EXPIRY_MINUTES = 10;

    public String cachePreview(StockOrderRequest request, StockOrderPreviewResponse previewResponse) {
        String previewId = UUID.randomUUID().toString();

        PreviewCacheItem cacheItem = new PreviewCacheItem(
                request,
                previewResponse,
                LocalDateTime.now().plusMinutes(PREVIEW_EXPIRY_MINUTES)
        );

        previewCache.put(previewId, cacheItem);
        log.debug("Önizleme önbelleğe kaydedildi. ID: {}", previewId);

        // Eski kayıtları temizle
        cleanExpiredCache();

        return previewId;
    }

    public void validatePreview(String previewId, StockOrderRequest request) {
        if (previewId == null || previewId.isEmpty()) {
            throw new ValidationException("Önizleme ID'si gereklidir. Önce ön izleme yapmalısınız.");
        }

        PreviewCacheItem cacheItem = previewCache.get(previewId);
        if (cacheItem == null) {
            throw new ValidationException("Geçersiz veya süresi dolmuş önizleme. Lütfen tekrar önizleme yapın.");
        }

        if (cacheItem.expiry.isBefore(LocalDateTime.now())) {
            previewCache.remove(previewId);
            throw new ValidationException("Önizleme süresi dolmuş. Lütfen tekrar önizleme yapın.");
        }

        // İstek verileri önizleme ile aynı mı kontrol et
        if (!isRequestMatchingPreview(cacheItem.request, request)) {
            throw new ValidationException("Satış bilgileri önizleme ile eşleşmiyor. Lütfen tekrar önizleme yapın.");
        }

        log.debug("Önizleme doğrulaması başarılı. ID: {}", previewId);
    }


    public void removePreview(String previewId) {
        previewCache.remove(previewId);
        log.debug("Önizleme önbellekten silindi. ID: {}", previewId);
    }


    private void cleanExpiredCache() {
        LocalDateTime now = LocalDateTime.now();
        previewCache.entrySet().removeIf(entry -> entry.getValue().expiry.isBefore(now));
    }


    private boolean isRequestMatchingPreview(StockOrderRequest previewRequest, StockOrderRequest executionRequest) {
        return previewRequest.getClientId().equals(executionRequest.getClientId()) &&
               previewRequest.getStockId().equals(executionRequest.getStockId()) &&
               previewRequest.getQuantity() == executionRequest.getQuantity() &&
               previewRequest.getExecutionType() == executionRequest.getExecutionType() &&
               // Limit emirleri için fiyat kontrolü
               (previewRequest.getPrice() == null && executionRequest.getPrice() == null ||
                previewRequest.getPrice() != null && executionRequest.getPrice() != null &&
                previewRequest.getPrice().compareTo(executionRequest.getPrice()) == 0);
    }

    private static class PreviewCacheItem {
        private final StockOrderRequest request;
        private final StockOrderPreviewResponse response;
        private final LocalDateTime expiry;

        public PreviewCacheItem(StockOrderRequest request, StockOrderPreviewResponse response, LocalDateTime expiry) {
            this.request = request;
            this.response = response;
            this.expiry = expiry;
        }
    }
}
