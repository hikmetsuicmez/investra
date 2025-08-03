package com.investra.service.helper;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.entity.*;
import com.investra.exception.*;
import com.investra.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityFinderService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final UserRepository userRepository;

    public OrderEntities findAndValidateEntities(StockSellOrderRequest request) {
        try {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(request.getClientId()));

            Stock stock = stockRepository.findById(request.getStockId())
                    .orElseThrow(() -> new StockNotFoundException("Geçersiz hisse senedi ID: " + request.getStockId()));

            PortfolioItem portfolioItem = portfolioItemRepository.findByClientIdAndStockId(client.getId(), stock.getId())
                    .orElseThrow(() -> new StockNotFoundException("Müşterinin portföyünde hisse senedi bulunamadı"));

            Account account = accountRepository.findByClientId(client.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Müşteri hesabı bulunamadı: " + client.getId()));

            if (portfolioItem.getQuantity() < request.getQuantity()) {
                throw new InsufficientStockException("Yetersiz hisse senedi miktarı: " + request.getQuantity());
            }

            return new OrderEntities(client, stock, portfolioItem, account);
        } catch (ClientNotFoundException | StockNotFoundException | AccountNotFoundException |
                InsufficientStockException e) {
            log.warn(e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Varlıklar aranırken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Varlıklar aranırken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Varlıklar aranırken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Varlıklar aranırken beklenmeyen bir hata oluştu", e);
        }
    }

    public User findUserByEmail(String userEmail) {
        try {
            return userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userEmail));
        } catch (UserNotFoundException e) {
            log.warn(e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Kullanıcı aranırken veritabanı hatası oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Kullanıcı aranırken bir hata oluştu", e);
        } catch (Exception e) {
            log.error("Kullanıcı aranırken beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new DatabaseOperationException("Kullanıcı aranırken beklenmeyen bir hata oluştu", e);
        }
    }
    public record OrderEntities(Client client, Stock stock, PortfolioItem portfolioItem, Account account) {}
}
