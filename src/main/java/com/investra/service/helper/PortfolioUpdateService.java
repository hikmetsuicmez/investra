package com.investra.service.helper;

import com.investra.entity.Account;
import com.investra.entity.PortfolioItem;
import com.investra.exception.DatabaseOperationException;
import com.investra.repository.AccountRepository;
import com.investra.repository.PortfolioItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioUpdateService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final AccountRepository accountRepository;

    public PortfolioItem updatePortfolioAfterPurchase(PortfolioItem portfolioItem, int quantity) {
        try {
            if (portfolioItem == null) {
                log.info("Portföy öğesi bulunamadı, yeni portföy öğesi oluşturuluyor.");
                portfolioItem = new PortfolioItem();
                portfolioItem.setQuantity(quantity);
            } else {
                log.info("Portföy öğesi güncelleniyor. ID: {}", portfolioItem.getId());
                portfolioItem.setQuantity(portfolioItem.getQuantity() + quantity);
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


    public void updateAccountBalanceAfterPurchase(Account account, BigDecimal amount) {
        try {
            account.setBalance(account.getBalance().subtract(amount));
            accountRepository.save(account);
        } catch (DataAccessException e) {
            log.error("Hesap bakiyesi güncellenirken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Hesap bakiyesi güncellenirken beklenmeyen bir hata oluştu", e);
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
}
