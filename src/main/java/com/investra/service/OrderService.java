package com.investra.service;

import com.investra.dtos.OrderCreateDto;
import com.investra.dtos.OrderDTO;
import com.investra.dtos.OrderUpdateDto;
import com.investra.entity.Account;
import com.investra.entity.Order;
import com.investra.entity.TradeOrder;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.enums.SettlementStatus;
import com.investra.exception.ResourceNotFoundException;
import com.investra.mapper.OrderMapper;
import com.investra.repository.AccountRepository;
import com.investra.repository.OrderRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.repository.UserRepository;
import com.investra.service.helper.PortfolioUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final AccountRepository accountRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final PortfolioUpdateService portfolioUpdateService;
    private final Random random = new Random();

    /**
     * Emir oluşturma - Bu metot StockBuy ve StockSell tarafından çağrılacak
     */
    @Transactional
    public OrderDTO createOrder(OrderCreateDto orderCreateDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderMapper.toEntity(orderCreateDto, user);

        // TradeOrder ID'sini ayarla
        order.setTradeOrderId(orderCreateDto.getTradeOrderId());

        // Toplam tutarı hesapla
        order.setTotalAmount(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        // Rastgele bir duruma atama - %60 bekleyen, %30 gerçekleşen, %10 iptal
        int randomValue = random.nextInt(100);

        if (randomValue < 60) {
            // %60 ihtimalle bekleyen emir
            order.setStatus(OrderStatus.PENDING);

            // Bekleyen emirler için 15 saniye sonra işlenecek şekilde ayarla (test için)
            order.setExpirationDate(LocalDateTime.now().plusSeconds(15));

            // Bekleyen emirlerde hesap bakiyesi güncellenmez
            // Sadece alış emirlerinde availableBalance güncellenir
            if (order.getOrderType() == OrderType.BUY) {
                updateAccountBalanceForPendingBuyOrder(orderCreateDto.getAccountId(), order.getTotalAmount());
            }

        } else if (randomValue < 90) {
            // %30 ihtimalle gerçekleşen emir
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(LocalDateTime.now());

            // T+2 sistemine göre 15 saniye sonra fonların serbest kalması (test için)
            order.setSettlementDate(LocalDateTime.now().plusSeconds(15));
            order.setSettlementStatus(SettlementStatus.PENDING); // Takas bekleniyor
            order.setFundsReserved(true);

            // Alış/satış işlemine göre hesap bakiyesini güncelle
            if (order.getOrderType() == OrderType.BUY) {
                // Alış işleminde, availableBalance azalır, total balance değişmez
                updateAccountBalanceForCompletedBuyOrder(orderCreateDto.getAccountId(), order.getTotalAmount());
            } else if (order.getOrderType() == OrderType.SELL) {
                // Satış işleminde, balance değişmez, T+2 sonrasında balance artar
                // Satış işlemlerinde herhangi bir bakiye değişikliği yok, sadece portfolyo güncellenir
                // Bakiye güncellemesi T+2 sonrasında yapılacak
            }

            // Bildirim gönder
            notificationService.sendOrderCompletedNotification(user, order);
        } else {
            // %10 ihtimalle iptal edilen emir
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());

            // İptal edilen emirlerde hesap bakiyesi değişmez

            // Bildirim gönder
            notificationService.sendOrderCancelledNotification(user, order);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toDto(savedOrder);
    }

    // Bekleyen alış emri için hesap bakiyesini günceller, * availableBalance azalır, total balance değişme
    private void updateAccountBalanceForPendingBuyOrder(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı"));

        // availableBalance'dan tutarı düş, balance aynı kalır
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));

        accountRepository.save(account);
        log.info("Bekleyen alış emri için availableBalance güncellendi: {}", accountId);
    }

    /**
     * Gerçekleşen alış emri için hesap bakiyesini günceller
     * availableBalance azalır, total balance değişmez
     */
    private void updateAccountBalanceForCompletedBuyOrder(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı"));

        // availableBalance'dan tutarı düş, balance aynı kalır
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));

        accountRepository.save(account);
        log.info("Gerçekleşen alış emri için availableBalance güncellendi: {}", accountId);
    }

    /**
     * T+2 süreci sonrasında satış emri için hesap bakiyesini günceller
     * balance ve availableBalance artar
     */
    private void updateAccountBalanceForSettledSellOrder(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı"));

        // balance ve availableBalance'a tutarı ekle
        account.setBalance(account.getBalance().add(amount));
        account.setAvailableBalance(account.getAvailableBalance().add(amount));

        accountRepository.save(account);
        log.info("Takas tamamlanan satış emri için balance ve availableBalance güncellendi: {}", accountId);
    }

    /**
     * T+2 süreci sonrasında alış emri için hesap bakiyesini günceller
     * balance azalır (availableBalance zaten düşürülmüştü)
     */
    private void updateAccountBalanceForSettledBuyOrder(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Hesap bulunamadı"));

        // Sadece balance'ı güncelle (availableBalance zaten düşürülmüştü)
        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.save(account);
        log.info("Takas tamamlanan alış emri için balance güncellendi: {}", accountId);
    }

    // Kullanıcının tüm emirlerini getir
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrdersByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orderMapper.toDtoList(orders);
    }

    // Duruma göre emirleri getir
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatusAndUser(String username, OrderStatus status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        List<Order> orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);
        return orderMapper.toDtoList(orders);
    }

    // Emir güncelleme
    @Transactional
    public OrderDTO updateOrder(Long orderId, OrderUpdateDto orderUpdateDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Emir bulunamadı"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bu emir size ait değil");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Sadece bekleyen emirler düzenlenebilir");
        }

        order.setQuantity(orderUpdateDto.getQuantity());
        order.setPrice(orderUpdateDto.getPrice());
        order.setTotalAmount(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDto(updatedOrder);
    }

    // Emir iptal etme
    @Transactional
    public OrderDTO cancelOrder(Long orderId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Emir bulunamadı"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Bu emir size ait değil");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Sadece bekleyen emirler iptal edilebilir");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        // Emir iptal edildiğinde bildirim gönder
        notificationService.sendOrderCancelledNotification(user, cancelledOrder);

        return orderMapper.toDto(cancelledOrder);
    }

    // Bekleyen emirleri getir
    @Transactional(readOnly = true)
    public List<OrderDTO> getPendingOrdersByUser(String username) {
        return getOrdersByStatusAndUser(username, OrderStatus.PENDING);
    }

    // Gerçekleşen emirleri getir
    @Transactional(readOnly = true)
    public List<OrderDTO> getCompletedOrdersByUser(String username) {
        return getOrdersByStatusAndUser(username, OrderStatus.COMPLETED);
    }

    // İptal edilen emirleri getir
    @Transactional(readOnly = true)
    public List<OrderDTO> getCancelledOrdersByUser(String username) {
        return getOrdersByStatusAndUser(username, OrderStatus.CANCELLED);
    }

    /**
     * Kısmen gerçekleşen emirleri getir
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getPartiallyCompletedOrdersByUser(String username) {
        return getOrdersByStatusAndUser(username, OrderStatus.PARTIALLY_COMPLETED);
    }

    // Bekleyen emirleri işleme - 15 saniye sonra gerçekleştirecek (test için)
    //     * Her 5 saniye çalışır (test için)
    @Scheduled(fixedRate = 5000) // Her 5 saniye kontrol et (test için)
    @Transactional
    public void processWaitingOrders() {
        log.info("Bekleyen emirlerin işlenmesi başlatılıyor...");
        LocalDateTime now = LocalDateTime.now();

        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .filter(order -> order.getExpirationDate() != null && now.isAfter(order.getExpirationDate()))
                .toList();

        for (Order order : pendingOrders) {
            log.info("Bekleyen emir işleniyor: {}", order.getId());

            // Emir gerçekleşti olarak işaretle
            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(now);

            // T+2 sistemi için takas tarihini ayarla (test için 15 saniye)
            order.setSettlementDate(now.plusSeconds(15));
            order.setSettlementStatus(SettlementStatus.PENDING);
            order.setFundsReserved(true);

            orderRepository.save(order);

            // Bildirim gönder
            notificationService.sendOrderCompletedNotification(order.getUser(), order);
        }

        if (!pendingOrders.isEmpty()) {
            log.info("{} adet bekleyen emir işlendi", pendingOrders.size());
        }
    }

    /**
     * T+2 sistemindeki emirlerin takasını tamamla
     * Her 5 saniye çalışır (test için)
     */
    @Scheduled(fixedRate = 5000) // Her 5 saniye kontrol et (test için)
    @Transactional
    public void settleCompletedOrders() {
        log.info("T+2 sistemi için emir takası başlatılıyor...");
        LocalDateTime now = LocalDateTime.now();

        List<Order> ordersToSettle = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .filter(order -> order.getSettlementStatus() == SettlementStatus.PENDING) // Takas bekleyen
                .filter(Order::isFundsReserved)
                .filter(order -> !order.isPortfolioUpdated()) // Portfolyo henüz güncellenmemiş
                .filter(order -> order.getSettlementDate() != null && now.isAfter(order.getSettlementDate()))
                .toList();

        for (Order order : ordersToSettle) {
            log.info("Emir takası tamamlanıyor: {}", order.getId());

            try {
                // İlgili TradeOrder'ı bul
                TradeOrder tradeOrder = null;
                if (order.getTradeOrderId() != null) {
                    tradeOrder = tradeOrderRepository.findById(order.getTradeOrderId())
                            .orElse(null);
                }

                // T+2 sonrası hesap bakiyesi güncelleme işlemleri
                if (tradeOrder != null) {
                    // Alış/satış işlemine göre farklı işlemler yap
                    if (order.getOrderType() == OrderType.BUY) {
                        // Alış işleminde T+2 sonrası total balance güncellenir
                        updateAccountBalanceForSettledBuyOrder(tradeOrder.getAccount().getId(), order.getTotalAmount());

                        // Portfolyo güncelleme - Alış işleminde hisseler portföye eklenir
                        // portfolioUpdateService.updatePortfolioForSettledBuy(tradeOrder);
                    } else if (order.getOrderType() == OrderType.SELL) {
                        // Satış işleminde T+2 sonrası hem total hem available balance artar
                        updateAccountBalanceForSettledSellOrder(tradeOrder.getAccount().getId(), order.getTotalAmount());

                        // Portfolyo güncelleme - Satış işleminde hisseler portföyden çıkarıldı
                        // Bu işlem zaten tamamlandı, sadece balance güncelleniyor
                    }

                    // TradeOrder durumunu güncelle - EXECUTED -> SETTLED
                    tradeOrder.setStatus(OrderStatus.SETTLED);
                    tradeOrder.setSettledAt(now);
                    tradeOrderRepository.save(tradeOrder);
                    log.info("TradeOrder takası tamamlandı: {}", tradeOrder.getId());
                }

                // Emirin durumunu güncelle
                order.setFundsReserved(false); // Fonları serbest bırak
                order.setSettlementStatus(SettlementStatus.COMPLETED); // Takas tamamlandı
                order.setPortfolioUpdated(true); // Portfolyo güncellendi

                orderRepository.save(order);

                log.info("Emir takası başarıyla tamamlandı: {}", order.getId());

            } catch (Exception e) {
                log.error("Emir takası sırasında hata oluştu: {}", e.getMessage(), e);
                // Hata durumunda bir sonraki emire geç
            }
        }

        if (!ordersToSettle.isEmpty()) {
            log.info("{} adet emirin takası tamamlandı", ordersToSettle.size());
        }
    }

    // Özel olarak bir emri gerçekleşen durumuna getir
    //     * StockBuy ve StockSell servislerinden doğrudan çağrılabilir
    @Transactional
    public OrderDTO completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Emir bulunamadı"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Sadece bekleyen emirler tamamlanabilir");
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setSettlementDate(LocalDateTime.now().plusDays(2));
        order.setFundsReserved(true);

        Order completedOrder = orderRepository.save(order);

        // Bildirim gönder
        notificationService.sendOrderCompletedNotification(completedOrder.getUser(), completedOrder);

        return orderMapper.toDto(completedOrder);
    }
}
