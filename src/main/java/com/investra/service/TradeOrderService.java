package com.investra.service;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Notification;
import com.investra.entity.Stock;
import com.investra.entity.TradeOrder;
import com.investra.entity.User;
import com.investra.enums.ExecutionType;
import com.investra.enums.NotificationType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.NotificationRepository;
import com.investra.repository.StockRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeOrderService {

    private final TradeOrderRepository tradeOrderRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final StockRepository stockRepository;
    private final PortfolioService portfolioService;

    /**
     * Bekleyen emirleri işleme - 15 saniye sonra gerçekleştirecek (test için)
     * Her 5 saniye çalışır (test için)
     */
    @Scheduled(fixedRate = 5000) // Her 5 saniye kontrol et
    public void processWaitingOrders() {
        log.info("Bekleyen emirlerin işlenmesi kontrol ediliyor... [{}]", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusSeconds(15); // 15 saniye öncesine kadar olan bekleyen emirler

        log.info("Cutoff zamanı: {}", cutoffTime);

        try {
            // PENDING durumundaki emirleri bul
            List<TradeOrder> pendingOrders = tradeOrderRepository.findByStatus(OrderStatus.PENDING);

            log.info("Bekleyen emir sayısı: {}", pendingOrders.size());

            for (TradeOrder order : pendingOrders) {
                // Emir tipine göre işlem yap
                if (order.getExecutionType() == ExecutionType.MARKET) {
                    // Market emirleri için zaman kontrolü yap
                    if (order.getSubmittedAt() != null && order.getSubmittedAt().isBefore(cutoffTime)) {
                        processWaitingOrder(order);
                        log.info("Market emri işlendi: {}", order.getId());
                    }
                } else if (order.getExecutionType() == ExecutionType.LIMIT) {
                    // Limit emirleri için fiyat kontrolü yap
                    processPendingLimitOrder(order);
                }
            }
        } catch (Exception e) {
            log.error("Bekleyen emirleri işlerken hata oluştu: {}", e.getMessage(), e);
        }
    }

    // Bekleyen bir limit emrini işler. Limit emirlerde, fiyat koşulları kontrol edilir ve uygun koşullar sağlandığında işlem gerçekleştirilir.
    @Transactional
    public void processPendingLimitOrder(TradeOrder order) {
        try {
            // Hisse senedinin güncel fiyatını al
            Stock stock = stockRepository.findById(order.getStock().getId())
                    .orElseThrow(() -> new RuntimeException("Hisse senedi bulunamadı: " + order.getStock().getId()));

            BigDecimal currentPrice = stock.getPrice();
            BigDecimal limitPrice = order.getPrice();

            // Fiyat koşullarını kontrol et
            boolean priceConditionMet = false;

            if (order.getOrderType() == OrderType.BUY) {
                // Alış limiti: Piyasa fiyatı <= Limit fiyatı
                priceConditionMet = currentPrice.compareTo(limitPrice) <= 0;
                if (priceConditionMet) {
                    log.info("Alış limit emri için fiyat koşulu sağlandı. Piyasa: {}, Limit: {}",
                            currentPrice, limitPrice);
                }
            } else if (order.getOrderType() == OrderType.SELL) {
                // Satış limiti: Piyasa fiyatı >= Limit fiyatı
                priceConditionMet = currentPrice.compareTo(limitPrice) >= 0;
                if (priceConditionMet) {
                    log.info("Satış limit emri için fiyat koşulu sağlandı. Piyasa: {}, Limit: {}",
                            currentPrice, limitPrice);
                }
            }

            // Koşullar sağlanıyorsa emri işle
            if (priceConditionMet) {
                processWaitingOrder(order);
                log.info("Limit emri başarıyla işlendi: ID={}, Fiyat={}", order.getId(), currentPrice);
            } else {
                log.debug("Limit emri için fiyat koşulları henüz sağlanmadı: ID={}, Piyasa Fiyatı={}, Limit Fiyatı={}",
                        order.getId(), currentPrice, limitPrice);
            }
        } catch (Exception e) {
            log.error("Limit emri {} işlenirken hata: {}", order.getId(), e.getMessage(), e);
        }
    }

    // Bekleyen bir emri işler
    @Transactional
    public void processWaitingOrder(TradeOrder order) {
        try {
            log.info("Bekleyen emir işleniyor: ID={}, SubmittedAt={}", order.getId(), order.getSubmittedAt());
            LocalDateTime now = LocalDateTime.now();

            // Emir gerçekleşti olarak işaretle
            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(now);

            // T+2 sistemi için takas tarihini ayarla (15 saniye sonra)
            order.setSettlementDate(now.plusSeconds(15)); // Test için 15 saniye
            order.setSettlementStatus(SettlementStatus.PENDING);
            order.setFundsReserved(true);

            tradeOrderRepository.save(order);

            try {
                sendOrderExecutedNotification(order.getUser(), order);
            } catch (Exception e) {
                log.error("Bildirim gönderme hatası (işlem etkilenmez): {}", e.getMessage());
            }

            log.info("Emir başarıyla gerçekleştirildi: {}", order.getId());
        } catch (Exception e) {
            log.error("Emir {} işlenirken hata: {}", order.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // T+2 sistemindeki emirlerin takasını tamamla
    //     * Her 5 saniye çalışır (test için)
    @Scheduled(fixedRate = 5000) // Her 5 saniye kontrol et
    public void settleCompletedOrders() {
        log.info("T+2 sistemi için takas kontrolü başlatılıyor... [{}]", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();

        try {
            // EXECUTED durumunda, PENDING settlement status'ünde ve settlement date'i geçmiş emirleri bul
            List<TradeOrder> ordersToSettle = tradeOrderRepository.findByStatusAndSettlementStatusAndSettlementDateBefore(
                    OrderStatus.EXECUTED, SettlementStatus.PENDING, now);

            log.info("Takas bekleyen emir sayısı: {}", ordersToSettle.size());

            for (TradeOrder order : ordersToSettle) {
                try {
                    settleCompletedOrder(order);
                } catch (Exception e) {
                    log.error("Emir {} takası sırasında hata, diğer emirlere devam ediliyor: {}",
                            order.getId(), e.getMessage());
                }
            }

            if (!ordersToSettle.isEmpty()) {
                log.info("{} adet emirin takası tamamlandı", ordersToSettle.size());
            }
        } catch (Exception e) {
            log.error("Emir takası sırasında genel hata oluştu: {}", e.getMessage(), e);
        }
    }

    // Tek bir emirin takasını tamamlar
    @Transactional
    public void settleCompletedOrder(TradeOrder order) {
        try {
            log.info("Emir takası tamamlanıyor: ID={}, SettlementDate={}", order.getId(), order.getSettlementDate());
            LocalDateTime now = LocalDateTime.now();

            Long orderId = order.getId();
            TradeOrder freshOrder = tradeOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Emir bulunamadı: " + orderId));

            Account account = accountRepository.findById(freshOrder.getAccount().getId())
                    .orElseThrow(() -> new RuntimeException("Hesap bulunamadı: " + freshOrder.getAccount().getId()));

            Stock stock = stockRepository.findById(freshOrder.getStock().getId())
                    .orElseThrow(() -> new RuntimeException("Hisse senedi bulunamadı: " + freshOrder.getStock().getId()));

            Client client = clientRepository.findById(freshOrder.getClient().getId())
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı: " + freshOrder.getClient().getId()));

            User user = null;
            if (freshOrder.getUser() != null) {
                Long userId = freshOrder.getUser().getId();
                user = userRepository.findById(userId)
                    .orElse(null);
            }

            // Emirin durumunu önce COMPLETED olarak güncelle
            freshOrder.setSettlementStatus(SettlementStatus.COMPLETED);
            freshOrder.setFundsReserved(false);
            freshOrder.setSettledAt(now);

            // Sadece COMPLETED olduktan sonra bakiyeyi güncelle
            if (freshOrder.getOrderType() == OrderType.BUY) {
                // Alış işlemi: T+2 sonunda balance'ı availableBalance seviyesine eşitle
                account.setBalance(account.getAvailableBalance());
                log.info("Alış T+2 tamamlandı: balance={} yapıldı", account.getAvailableBalance());
            } else if (freshOrder.getOrderType() == OrderType.SELL) {
                // Satış işlemi: T+2 sonunda hem balance hem availableBalance artırılır
                account.setBalance(account.getBalance().add(freshOrder.getNetAmount()));
                account.setAvailableBalance(account.getAvailableBalance().add(freshOrder.getNetAmount()));
                log.info("Satış T+2 tamamlandı: her ikisine de {} eklendi", freshOrder.getNetAmount());
            }

            accountRepository.save(account);

            // Sadece COMPLETED olduktan sonra portfolioyu güncelle
            // Portfolio güncellemesi yapalım (PortfolioService üzerinden)
            try {
                // Proxy nesneler yerine taze yüklenmiş nesneleri gönder
                portfolioService.updatePortfolioWithEntities(
                    freshOrder.getId(),
                    freshOrder.getOrderType(),
                    freshOrder.getQuantity(),
                    freshOrder.getPrice(),
                    stock,
                    account,
                    client
                );
                freshOrder.setPortfolioUpdated(true);
                log.info("Emir için portfolio güncellendi: {}", freshOrder.getId());
            } catch (Exception e) {
                log.error("Portfolio güncellenirken hata: {}", e.getMessage(), e);
                freshOrder.setPortfolioUpdated(false);
            }

            tradeOrderRepository.save(freshOrder);

            if (user != null) {
                try {
                    sendOrderSettledNotification(user, stock.getSymbol(), freshOrder.getOrderType());
                    log.info("Bildirim gönderildi: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("Bildirim gönderme hatası (işlem etkilenmez): {}", e.getMessage());
                }
            } else {
                log.warn("Kullanıcı bulunamadığı için bildirim gönderilemedi. Order ID: {}", freshOrder.getId());
            }

            log.info("Emir takası başarıyla tamamlandı: {}", freshOrder.getId());
        } catch (Exception e) {
            log.error("Emir {} takası sırasında hata: {}", order.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // Emir gerçekleştiğinde bildirim gönderir
    private void sendOrderExecutedNotification(User user, TradeOrder order) {
        if (user == null || order == null || order.getStock() == null) {
            log.warn("Bildirim gönderilemedi: Eksik kullanıcı veya emir bilgisi");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .recipient(user.getEmail()) // Kullanıcı adı yerine e-posta adresi kullanılıyor
                    .subject("Emir Gerçekleşti")
                    .content(order.getStock().getSymbol() + " hissesi için " + order.getOrderType().name() +
                            " emriniz başarıyla gerçekleştirildi.")
                    .type(NotificationType.INFO)
                    .isHtml(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Bildirim başarıyla gönderildi: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Bildirim gönderme hatası: {}", e.getMessage(), e);
        }
    }

    //  Emir takası tamamlandığında bildirim gönderir
    private void sendOrderSettledNotification(User user, String stockSymbol, OrderType orderType) {
        if (user == null || stockSymbol == null || orderType == null) {
            log.warn("Bildirim gönderilemedi: Eksik bilgi");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .recipient(user.getEmail()) // Kullanıcı adı yerine e-posta adresi kullanılıyor
                    .subject("Emir Takası Tamamlandı")
                    .content(stockSymbol + " hissesi için " + orderType.name() +
                            " emirinizin takası tamamlandı ve hesap bakiyeniz güncellendi.")
                    .type(NotificationType.INFO)
                    .isHtml(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Takas bildirimi başarıyla gönderildi: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Bildirim gönderme hatası: {}", e.getMessage(), e);
        }
    }

    // Kullanıcının tüm emirlerini getirir
    @Transactional(readOnly = true)
    public List<TradeOrder> getAllOrdersByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        return tradeOrderRepository.findByUserOrderBySubmittedAtDesc(user);
    }

    // Kullanıcının belirli durumdaki emirlerini getirir
    @Transactional(readOnly = true)
    public List<TradeOrder> getOrdersByStatusAndUser(String username, OrderStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıc�� bulunamadı"));

        return tradeOrderRepository.findByUserAndStatusOrderBySubmittedAtDesc(user, status);
    }

    // Bekleyen bir emri iptal eder
    @Transactional
    public TradeOrder cancelOrder(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        TradeOrder order = tradeOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Emir bulunamadı"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu emir size ait değil");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Sadece bekleyen emirler iptal edilebilir");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // İptal edilen emirler hiçbir şekilde portföye yansıtılmamalı
        order.setPortfolioUpdated(false);

        // Settlement status'ü iptal olarak işaretle, böylece takasa girmeyecek
        order.setSettlementStatus(SettlementStatus.CANCELLED);

        // Alış emri iptal edildiğinde availableBalance'ı güncelle
        if (order.getOrderType() == OrderType.BUY) {
            restoreAccountBalanceForCancelledBuyOrder(order.getAccount(), order.getNetAmount());
        }

        TradeOrder cancelledOrder = tradeOrderRepository.save(order);

        Notification notification = Notification.builder()
                .recipient(user.getEmail()) // Kullanıcı adı yerine e-posta adresi kullanılıyor
                .subject("Emir İptal Edildi")
                .content(order.getStock().getSymbol() + " hissesi için " + order.getOrderType().name() +
                        " emiriniz iptal edildi.")
                .type(NotificationType.INFO)
                .isHtml(false) // isHtml alanı eklendi
                .build();

        notificationRepository.save(notification);
        log.info("İptal bildirimi başarıyla gönderildi: {}", user.getEmail());

        return cancelledOrder;
    }

    // Alış işleminde hesap bakiyesini günceller
    //  AvailableBalance azalır, total balance değişmez
    @Transactional
    public void updateAccountBalanceForBuyOrder(Account account, BigDecimal amount) {
        // availableBalance'dan tutarı düş, balance aynı kalır
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Alış emri için availableBalance güncellendi: {}", account.getId());
    }

    // İptal edilen alış emirleri için hesap bakiyesini geri alır
    //  AvailableBalance artırılır
    @Transactional
    public void restoreAccountBalanceForCancelledBuyOrder(Account account, BigDecimal amount) {
        // availableBalance'a tutarı geri ekle
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        accountRepository.save(account);
        log.info("İptal edilen alış emri için availableBalance geri alındı: {}", account.getId());
    }
}
