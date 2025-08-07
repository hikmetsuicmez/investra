package com.investra.service;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Portfolio;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.entity.TradeOrder;
import com.investra.enums.OrderType;
import com.investra.exception.AccountNotFoundException;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.StockNotFoundException;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.PortfolioRepository;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    // Takas işlemi tamamlandıktan sonra portföyü günceller
    @Transactional
    public void updatePortfolioAfterSettlement(TradeOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Emir bilgisi boş olamaz");
        }
        Long stockId = order.getStock().getId();
        String stockCode = order.getStock().getCode();
        Long accountId = order.getAccount().getId();
        Long clientId = order.getClient().getId();

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException("Hisse senedi bulunamadı: " + stockId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: " + accountId));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Müşteri bulunamadı: " + clientId));

        log.info("Portföy güncellemesi başlatılıyor. Emir ID: {}, Hisse: {}, Client ID: {}",
                 order.getId(), stockCode, clientId);

        // Client'a ait portföyü bul veya oluştur
        Portfolio portfolio;
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByClient(order.getClient());

        if (existingPortfolio.isPresent()) {
            portfolio = existingPortfolio.get();
            log.info("Müşteriye ait mevcut portföy bulundu: {}", portfolio.getId());
        } else {
            log.info("Müşteriye ait portföy bulunamadı, yeni oluşturuluyor. Client ID: {}", order.getClient().getId());
            portfolio = Portfolio.builder()
                    .client(order.getClient())
                    .createdAt(LocalDateTime.now())
                    .build();
            portfolio = portfolioRepository.save(portfolio);
            log.info("Yeni portföy oluşturuldu: {}", portfolio.getId());
        }

        log.info("Portföyde hisse aranıyor. Client ID: {}, Stock ID: {}",
                 order.getClient().getId(), stock.getId());

        Optional<PortfolioItem> existingItemOpt;
        try {
            existingItemOpt = portfolioItemRepository.findByClientIdAndStockId(
                    order.getClient().getId(), stock.getId());
            log.info("Portföy öğesi bulundu mu: {}", existingItemOpt.isPresent());
        } catch (Exception e) {
            log.error("Portföy öğesi aranırken hata: {}", e.getMessage(), e);
            throw e;
        }

        try {
            if (order.getOrderType() == OrderType.BUY) {
                processBuyOrder(order, existingItemOpt, portfolio, account, stock);
            } else if (order.getOrderType() == OrderType.SELL) {
                processSellOrder(order, existingItemOpt, portfolio, account, stock);
            } else {
                log.warn("Bilinmeyen emir tipi: {}", order.getOrderType());
            }
        } catch (Exception e) {
            log.error("Portföy işlemi sırasında hata: {}", e.getMessage(), e);
            throw e;
        }

        log.info("Portföy güncellemesi tamamlandı. Emir ID: {}", order.getId());
    }

    // Alış emri için portföy güncellemesi yapar
    private void processBuyOrder(TradeOrder order, Optional<PortfolioItem> existingItemOpt,
                                Portfolio portfolio, Account account, Stock stock) {
        int quantity = order.getQuantity();
        BigDecimal price = order.getPrice();
        LocalDateTime now = LocalDateTime.now();

        log.info("Alış emri işleniyor: Miktar={}, Fiyat={}", quantity, price);

        if (existingItemOpt.isPresent()) {
            // Mevcut portföy kaydını güncelle
            PortfolioItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;

            BigDecimal totalValue = existingItem.getAvgPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity()))
                    .add(price.multiply(BigDecimal.valueOf(quantity)));
            BigDecimal newAvgPrice = totalValue.divide(BigDecimal.valueOf(newQuantity), 2, BigDecimal.ROUND_HALF_UP);

            log.info("Mevcut portföy öğesi güncelleniyor: ID={}, Eski Miktar={}, Yeni Miktar={}, Eski Fiyat={}, Yeni Fiyat={}",
                    existingItem.getId(), existingItem.getQuantity(), newQuantity, existingItem.getAvgPrice(), newAvgPrice);

            existingItem.setQuantity(newQuantity);
            existingItem.setAvgPrice(newAvgPrice);
            existingItem.setLastUpdated(now);

            try {
                PortfolioItem saved = portfolioItemRepository.save(existingItem);
                log.info("Portföy öğesi güncellendi: ID={}", saved.getId());
            } catch (Exception e) {
                log.error("Portföy öğesi güncellenirken hata: {}", e.getMessage(), e);
                throw e;
            }

            log.info("Portföydeki mevcut hisse güncellendi: {}, Yeni Miktar: {}, Yeni Ort. Fiyat: {}",
                    stock.getCode(), newQuantity, newAvgPrice);
        } else {
            // Yeni portföy kaydı oluştur
            try {
                log.info("Yeni portföy öğesi oluşturuluyor: Portfolio ID={}, Account ID={}, Stock ID={}",
                        portfolio.getId(), account.getId(), stock.getId());

                PortfolioItem newItem = PortfolioItem.builder()
                        .portfolio(portfolio)
                        .account(account)
                        .stock(stock)
                        .quantity(quantity)
                        .avgPrice(price)
                        .lastUpdated(now)
                        .build();

                PortfolioItem saved = portfolioItemRepository.save(newItem);
                log.info("Yeni portföy öğesi kaydedildi: ID={}", saved.getId());
            } catch (Exception e) {
                log.error("Yeni portföy öğesi kaydedilirken hata: {}", e.getMessage(), e);
                throw e;
            }

            log.info("Portföye yeni hisse eklendi: {}, Miktar: {}, Fiyat: {}",
                    stock.getCode(), quantity, price);
        }
    }

    // Satış emri için portföy güncellemesi yapar
    private void processSellOrder(TradeOrder order, Optional<PortfolioItem> existingItemOpt,
                                 Portfolio portfolio, Account account, Stock stock) {
        int quantity = order.getQuantity();

        if (existingItemOpt.isPresent()) {
            PortfolioItem existingItem = existingItemOpt.get();
            int currentQuantity = existingItem.getQuantity();

            if (currentQuantity < quantity) {
                throw new IllegalStateException("Portföyde yeterli hisse senedi bulunmuyor: " + stock.getCode());
            }

            int newQuantity = currentQuantity - quantity;

            if (newQuantity > 0) {
                // Hala hisse kaldıysa güncelle, fiyat değişmez
                existingItem.setQuantity(newQuantity);
                existingItem.setLastUpdated(LocalDateTime.now());
                portfolioItemRepository.save(existingItem);
                log.info("Portföydeki hisse miktarı azaltıldı: {}, Yeni Miktar: {}",
                        stock.getCode(), newQuantity);
            } else {
                // Hisse kalmadıysa kaydı sil
                portfolioItemRepository.delete(existingItem);
                log.info("Portföyden hisse tamamen çıkarıldı: {}", stock.getCode());
            }
        } else {
            throw new IllegalStateException("Portföyde satılacak hisse senedi bulunamadı: " + stock.getCode());
        }
    }

    // Entity'leri doğrudan alarak portfolyo güncellemesi yapar.
    @Transactional
    public void updatePortfolioWithEntities(
            Long orderId,
            OrderType orderType,
            Integer quantity,
            BigDecimal price,
            Stock stock,
            Account account,
            Client client) {

        log.info("Portföy güncellemesi başlatılıyor (Entity'ler ile). Emir ID: {}, Hisse: {}, Client ID: {}",
                orderId, stock.getCode(), client.getId());

        // Client'a ait portföyü bul veya oluştur
        Portfolio portfolio;
        Optional<Portfolio> existingPortfolio = portfolioRepository.findByClient(client);

        if (existingPortfolio.isPresent()) {
            portfolio = existingPortfolio.get();
            log.info("Müşteriye ait mevcut portföy bulundu: {}", portfolio.getId());
        } else {
            log.info("Müşteriye ait portföy bulunamadı, yeni oluşturuluyor. Client ID: {}", client.getId());
            portfolio = Portfolio.builder()
                    .client(client)
                    .createdAt(LocalDateTime.now())
                    .build();
            portfolio = portfolioRepository.save(portfolio);
            log.info("Yeni portföy oluşturuldu: {}", portfolio.getId());
        }

        // Portföyde bu hisseye ait bir kayıt var mı kontrol et
        log.info("Portföyde hisse aranıyor. Client ID: {}, Stock ID: {}", client.getId(), stock.getId());

        Optional<PortfolioItem> existingItemOpt;
        try {
            existingItemOpt = portfolioItemRepository.findByClientIdAndStockId(client.getId(), stock.getId());
            log.info("Portföy öğesi bulundu mu: {}", existingItemOpt.isPresent());
        } catch (Exception e) {
            log.error("Portföy öğesi aranırken hata: {}", e.getMessage(), e);
            throw e;
        }

        LocalDateTime now = LocalDateTime.now();

        try {
            if (orderType == OrderType.BUY) {
                if (existingItemOpt.isPresent()) {
                    PortfolioItem existingItem = existingItemOpt.get();
                    int newQuantity = existingItem.getQuantity() + quantity;

                    BigDecimal totalValue = existingItem.getAvgPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity()))
                            .add(price.multiply(BigDecimal.valueOf(quantity)));
                    BigDecimal newAvgPrice = totalValue.divide(BigDecimal.valueOf(newQuantity), 2, BigDecimal.ROUND_HALF_UP);

                    log.info("Mevcut portföy öğesi güncelleniyor: ID={}, Eski Miktar={}, Yeni Miktar={}, Eski Fiyat={}, Yeni Fiyat={}",
                            existingItem.getId(), existingItem.getQuantity(), newQuantity, existingItem.getAvgPrice(), newAvgPrice);

                    existingItem.setQuantity(newQuantity);
                    existingItem.setAvgPrice(newAvgPrice);
                    existingItem.setLastUpdated(now);

                    PortfolioItem saved = portfolioItemRepository.save(existingItem);
                    log.info("Portföy öğesi güncellendi: ID={}", saved.getId());
                } else {
                    // Yeni portföy kaydı oluştur
                    log.info("Yeni portföy öğesi oluşturuluyor: Portfolio ID={}, Account ID={}, Stock ID={}",
                            portfolio.getId(), account.getId(), stock.getId());

                    PortfolioItem newItem = PortfolioItem.builder()
                            .portfolio(portfolio)
                            .account(account)
                            .stock(stock)
                            .quantity(quantity)
                            .avgPrice(price)
                            .lastUpdated(now)
                            .build();

                    PortfolioItem saved = portfolioItemRepository.save(newItem);
                    log.info("Yeni portföy öğesi kaydedildi: ID={}", saved.getId());
                }
                log.info("Alış emri portföye eklendi: {}, Miktar: {}, Fiyat: {}", stock.getCode(), quantity, price);
            } else if (orderType == OrderType.SELL) {
                if (existingItemOpt.isPresent()) {
                    PortfolioItem existingItem = existingItemOpt.get();
                    int currentQuantity = existingItem.getQuantity();

                    if (currentQuantity < quantity) {
                        throw new IllegalStateException("Portföyde yeterli hisse senedi bulunmuyor: " + stock.getCode());
                    }

                    int newQuantity = currentQuantity - quantity;

                    if (newQuantity > 0) {
                        existingItem.setQuantity(newQuantity);
                        existingItem.setLastUpdated(now);
                        portfolioItemRepository.save(existingItem);
                        log.info("Portföydeki hisse miktarı azaltıldı: {}, Yeni Miktar: {}",
                                stock.getCode(), newQuantity);
                    } else {
                        // Hisse kalmadıysa kaydı sil
                        portfolioItemRepository.delete(existingItem);
                        log.info("Portföyden hisse tamamen çıkarıldı: {}", stock.getCode());
                    }
                } else {
                    throw new IllegalStateException("Portföyde satılacak hisse senedi bulunamadı: " + stock.getCode());
                }
                log.info("Satış emri portföye yansıtıldı: {}, Miktar: {}", stock.getCode(), quantity);
            } else {
                log.warn("Bilinmeyen emir tipi: {}", orderType);
            }
        } catch (Exception e) {
            log.error("Portföy işlemi sırasında hata: {}", e.getMessage(), e);
            throw e;
        }

        log.info("Portföy güncellemesi tamamlandı. Emir ID: {}", orderId);
    }
}
