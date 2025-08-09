package com.investra.service.helper;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Portfolio;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.exception.DatabaseOperationException;
import com.investra.repository.AccountRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdateService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;

    public PortfolioItem updatePortfolioAfterSell(PortfolioItem portfolioItem, int quantity) {
        try {
            if (portfolioItem.getQuantity() == quantity) {
                log.info("Tüm hisseler satıldı, portföy öğesi siliniyor. ID: {}",
                        portfolioItem.getId());
                portfolioItemRepository.delete(portfolioItem);
                return null;
            } else {
                portfolioItem.setQuantity(portfolioItem.getQuantity() - quantity);
                return portfolioItemRepository.save(portfolioItem);
            }
        } catch (DataAccessException e) {
            log.error("Portföy güncellenirken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Portföy güncellenirken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Portföy güncellenirken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Portföy güncellenirken beklenmeyen bir hata oluştu", e);
        }
    }

    public void updateAccountBalanceAfterSell(Account account, BigDecimal amount) {
        try {
            account.setBalance(account.getBalance().add(amount));
            accountRepository.save(account);
        } catch (DataAccessException e) {
            log.error("Hesap bakiyesi güncellenirken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu", e);
        }
    }

    public void updatePortfolioForBuy(Client client, Account account, Stock stock, int quantity, BigDecimal totalAmount) {
        try {
            // Hesap bakiyesi güncellenir (tutar düşülür)
            updateAccountBalanceAfterBuy(account, totalAmount);

            // Portföy güncellenir
            updateOrCreatePortfolioItem(client, stock, quantity);

            log.info("Alış işlemi sonrası portföy ve hesap bakiyesi güncellendi. Müşteri ID: {}, Hisse: {}, Miktar: {}, Tutar: {}",
                    client.getId(), stock.getCode(), quantity, totalAmount);
        } catch (Exception e) {
            log.error("Alış işlemi sonrası güncelleme yapılırken hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Alış işlemi sonrası güncelleme yapılırken hata oluştu", e);
        }
    }

    private void updateAccountBalanceAfterBuy(Account account, BigDecimal amount) {
        try {
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
            log.debug("Hesap bakiyesi güncellendi: {} - {}", account.getAccountNumber(), amount);
        } catch (DataAccessException e) {
            log.error("Hesap bakiyesi güncellenirken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu", e);
        }
    }

    private PortfolioItem updateOrCreatePortfolioItem(Client client, Stock stock, int quantity) {
        try {
            // Müşterinin portföyünde bu hisse senedi var mı kontrol edilir
            PortfolioItem portfolioItem = portfolioItemRepository
                    .findByClientIdAndStockId(client.getId(), stock.getId())
                    .orElse(null);

            if (portfolioItem != null) {
                // Varsa miktarı güncellenir
                int newQuantity = portfolioItem.getQuantity() + quantity;
                portfolioItem.setQuantity(newQuantity);
                portfolioItem.setLastUpdated(LocalDateTime.now());
                log.debug("Portföy öğesi güncellendi. ID: {}, Önceki miktar: {}, Yeni miktar: {}",
                        portfolioItem.getId(), portfolioItem.getQuantity() - quantity, newQuantity);
            } else {
                // Yoksa yeni bir portföy öğesi oluşturulur

                // DÜZELTME: List olarak al ve ilkini kullan
                List<Portfolio> portfolios = portfolioRepository.findAllByClientId(client.getId());

                if (portfolios.isEmpty()) {
                    throw new IllegalStateException("Müşterinin portföyü bulunamadı: " + client.getId());
                }

                if (portfolios.size() > 1) {
                    log.warn("Müşteri için birden fazla portföy bulundu: {}. İlki kullanılıyor.", client.getId());
                }

                Portfolio portfolio = portfolios.get(0); // İlk portföyü kullan

                // Müşterinin hesabını repository üzerinden bulalım
                Account account = accountRepository.findByClientId(client.getId())
                        .orElseThrow(() -> new IllegalStateException("Müşterinin hesabı bulunamadı: " + client.getId()));

                portfolioItem = PortfolioItem.builder()
                        .account(account)
                        .portfolio(portfolio)
                        .stock(stock)
                        .quantity(quantity)
                        .avgPrice(stock.getPrice()) // Ortalama fiyat olarak güncel fiyatı alıyoruz
                        .lastUpdated(LocalDateTime.now())
                        .build();
                log.debug("Yeni portföy öğesi oluşturuldu. Müşteri: {}, Hisse: {}, Miktar: {}",
                        client.getId(), stock.getCode(), quantity);
            }

            return portfolioItemRepository.save(portfolioItem);
        } catch (DataAccessException e) {
            log.error("Portföy güncellenirken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Portföy güncellenirken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Portföy güncellenirken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Portföy güncellenirken beklenmeyen bir hata oluştu", e);
        }
    }
}
