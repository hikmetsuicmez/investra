package com.investra.service.helper;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.entity.*;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.*;
import com.investra.repository.*;
import com.investra.service.helper.record.OrderCalculation;
import com.investra.service.helper.record.OrderEntities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityFinderService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final UserRepository userRepository;
    private final OrderCalculationService calculationService;

    public OrderEntities findAndValidateEntities(StockOrderRequest request) {
        try {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(request.getClientId()));

            Stock stock = stockRepository.findById(request.getStockId())
                    .orElseThrow(() -> new StockNotFoundException("Geçersiz hisse senedi ID: " + request.getStockId()));

            PortfolioItem portfolioItem = portfolioItemRepository.findByClientIdAndStockId(client.getId(), stock.getId())
                    .orElseThrow(() -> new StockNotFoundException("Müşterinin portföyünde hisse senedi bulunamadı"));

            Account account = accountRepository.findByClientId(client.getId())
                    .orElseThrow(() -> new AccountNotFoundException("Müşteri hesabı bulunamadı: " + client.getId()));

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

    public void validatePortfolioQuantity(PortfolioItem portfolioItem, int requiredQuantity) {
        if (portfolioItem.getQuantity() < requiredQuantity) {
            throw new InsufficientStockException("Yetersiz hisse senedi miktarı: " + requiredQuantity);
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

    public OrderEntities getEntities(StockOrderRequest request) {
        return findAndValidateEntities(request);
    }

    public TradeOrder buildTradeOrder(OrderEntities entities, StockOrderRequest request, OrderCalculation calculation, User submittedBy) {
        return TradeOrder.builder()
                .client(entities.client())
                .account(entities.account())
                .stock(entities.stock())
                .orderType(OrderType.SELL)
                .quantity(request.getQuantity())
                .price(calculation.unitPrice())
                .totalAmount(calculation.totalAmount())
                .status(OrderStatus.EXECUTED)
                .executionType(request.getExecutionType())
                .user(submittedBy)
                .submittedAt(LocalDateTime.now())
                .executedAt(LocalDateTime.now().plusDays(2))
                .build();
    }

    public List<Object> processOrder(StockOrderRequest request, String userEmail) {
        List<Object> responses = new ArrayList<>();

        OrderEntities entities = getEntities(request);
        responses.add(entities);
        OrderCalculation calculation = calculationService.getCalculation(request, entities);
        responses.add(calculation);
        TradeOrder tradeOrder = buildTradeOrder(entities, request, calculation, findUserByEmail(userEmail));
        responses.add(tradeOrder);
        User submittedBy = findUserByEmail(userEmail);
        responses.add(submittedBy);
        return responses;

    }
}

