package com.investra.service.helper;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.StockSellOrderPreviewResponse;
import com.investra.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Satış ve alış işlemleri için ön izleme önbelleğini yöneten servis
 */
@Component
@Slf4j
public class OrderPreviewCacheService {

    // Önizleme önbelleği: Anahtar = Önizleme ID, Değer = Önizleme detayları
    private final Map<String, PreviewCacheItem> previewCache = new ConcurrentHashMap<>();

    // Önizleme kaydının geçerlilik süresi (dakika)
    private static final int PREVIEW_EXPIRY_MINUTES = 10;

    public String cachePreview(StockSellOrderRequest request, StockSellOrderPreviewResponse previewResponse) {
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

    public String cacheOrderPreview(String previewId, Object request) {
        if (previewId == null) {
            previewId = UUID.randomUUID().toString();
        }

        PreviewCacheItem cacheItem = new PreviewCacheItem(
                request,
                null,
                LocalDateTime.now().plusMinutes(PREVIEW_EXPIRY_MINUTES)
        );

        previewCache.put(previewId, cacheItem);
        log.debug("Sipariş önizlemesi önbelleğe alındı. ID: {}, İstek türü: {}",
                previewId, request.getClass().getSimpleName());

        // Eski kayıtları temizle
        cleanExpiredCache();

        return previewId;
    }

    public StockSellOrderRequest getPreviewRequest(String previewId) {
        PreviewCacheItem cacheItem = previewCache.get(previewId);

        if (cacheItem == null) {
            log.warn("Önizleme bulunamadı: {}", previewId);
            throw new ValidationException("Önizleme bulunamadı veya süresi doldu");
        }

        if (cacheItem.isExpired()) {
            log.warn("Önizleme süresi doldu: {}", previewId);
            previewCache.remove(previewId);
            throw new ValidationException("Önizleme süresi doldu");
        }

        if (!(cacheItem.getRequest() instanceof StockSellOrderRequest)) {
            log.warn("Önizleme yanlış türde: {}", previewId);
            throw new ValidationException("Önizleme yanlış türde");
        }

        return (StockSellOrderRequest) cacheItem.getRequest();
    }

    public Object getOrderPreview(String previewId) {
        PreviewCacheItem cacheItem = previewCache.get(previewId);

        if (cacheItem == null) {
            log.warn("Önizleme bulunamadı: {}", previewId);
            return null;
        }

        if (cacheItem.isExpired()) {
            log.warn("Önizleme süresi doldu: {}", previewId);
            previewCache.remove(previewId);
            return null;
        }

        return cacheItem.getRequest();
    }

    public void removeOrderPreview(String previewId) {
        previewCache.remove(previewId);
        log.debug("Önizleme önbellekten silindi: {}", previewId);
    }

    private void cleanExpiredCache() {
        previewCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }


    private static class PreviewCacheItem {
        private final Object request;
        private final Object response;
        private final LocalDateTime expiryTime;

        public PreviewCacheItem(Object request, Object response, LocalDateTime expiryTime) {
            this.request = request;
            this.response = response;
            this.expiryTime = expiryTime;
        }

        public Object getRequest() {
            return request;
        }

        public Object getResponse() {
            return response;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
