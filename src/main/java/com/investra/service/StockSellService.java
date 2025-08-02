package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.repository.*;
import com.investra.service.helper.StockTradeHelper;
import com.investra.service.helper.StockTradeHelper.TradeCalculation;
import com.investra.service.helper.StockTradeHelper.TradeEntities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockSellService {
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockTradeHelper tradeHelper;

    public List<ClientSearchResponse> searchClients(ClientSearchRequest request) {
        List<Client> clients;

        switch (request.getSearchType()) {
            case "TCKN":
                clients = clientRepository.findByTckn(request.getSearchTerm())
                        .map(List::of).orElse(List.of());
                break;
            case "VERGI_NO":
                clients = clientRepository.findByVergiNo(request.getSearchTerm())
                        .map(List::of).orElse(List.of());
                break;
            case "BLUE_CARD_NO":
                clients = clientRepository.findByBlueCardNo(request.getSearchTerm())
                        .map(List::of).orElse(List.of());
                break;
            case "ACCOUNT_NO":
                clients = clientRepository.findByAccountNumber(request.getSearchTerm())
                        .map(List::of).orElse(List.of());
                break;
            case "FULL_NAME":
                clients = clientRepository.findByFullNameContainingIgnoreCase(request.getSearchTerm());
                break;
            default:
                clients = clientRepository.searchClients(request.getSearchTerm());
        }

        return clients.stream()
                .map(this::mapToClientSearchResponse)
                .collect(Collectors.toList());
    }

    public List<ClientStockHoldingResponse> getClientStockHoldings(Long clientId) {
        return portfolioItemRepository.findByClientId(clientId).stream()
                .map(this::mapToClientStockHoldingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StockSellOrderPreviewResponse previewSellOrder(StockSellOrderRequest request) {
        // İstek validasyonu
        tradeHelper.validateSellOrderRequest(request);

        // İşlem için gerekli varlıkları bul
        TradeEntities entities = tradeHelper.findTradeEntities(request);
        Client client = entities.client();
        Stock stock = entities.stock();
        PortfolioItem portfolioItem = entities.portfolioItem();
        Account account = entities.account();

        // Fiyat ve işlem tutarlarını hesapla
        BigDecimal price = tradeHelper.calculatePrice(request, stock);
        TradeCalculation calculation = tradeHelper.calculateTradeAmounts(price, request.getQuantity(), client);

        // Yanıt oluştur
        return StockSellOrderPreviewResponse.builder()
                .accountNumber(account.getAccountNumber())
                .operation("Satış")
                .stockName(stock.getName())
                .stockSymbol(stock.getSymbol())
                .price(price)
                .quantity(request.getQuantity())
                .tradeDate(tradeHelper.getTradeDate())
                .valueDate(tradeHelper.calculateValueDate())
                .totalAmount(calculation.totalAmount())
                .stockGroup(stock.getGroup())
                .commission(calculation.commission())
                .bsmv(calculation.bsmv())
                .totalTaxAndCommission(calculation.totalTaxAndCommission())
                .netAmount(calculation.netAmount())
                .executionType(request.getExecutionType())
                .build();
    }

    @Transactional
    public StockSellOrderResultResponse executeSellOrder(StockSellOrderRequest request, String userEmail) {
        if (!request.getPreviewConfirmed()) {
            throw new RuntimeException("Emir önizlemesi onaylanmamış");
        }

        // Ön izleme ile aynı doğrulamaları ve hesaplamaları yap
        StockSellOrderPreviewResponse preview = previewSellOrder(request);

        // İşlem için gerekli varlıkları bul
        TradeEntities entities = tradeHelper.findTradeEntities(request);
        Stock stock = entities.stock();

        // Kullanıcıyı bul
        User submittedBy = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        LocalDateTime now = LocalDateTime.now();
        String orderReference = generateOrderReference();

        // Emir oluştur
        TradeOrder order = TradeOrder.builder()
                .client(clientRepository.getReferenceById(request.getClientId()))
                .account(accountRepository.getReferenceById(request.getAccountId()))
                .stock(stock)
                .stockCode(stock.getSymbol())
                .orderReference(orderReference)
                .orderType(OrderType.SELL)
                .executionType(request.getExecutionType())
                .price(preview.getPrice())
                .quantity(request.getQuantity())
                .totalAmount(preview.getTotalAmount())
                .commission(preview.getCommission())
                .bsmv(preview.getBsmv())
                .netAmount(preview.getNetAmount())
                .valueDate(preview.getValueDate())
                .status(OrderStatus.PENDING)
                .submittedBy(submittedBy)
                .orderDate(now)
                .submittedAt(now)
                .build();

        try {
            // Emir kontrolü ve işleme alma
            validateOrderExecution(order);

            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(LocalDateTime.now());

            // Portföy güncelleme
            updatePortfolio(order);

            TradeOrder savedOrder = tradeOrderRepository.save(order);

            return StockSellOrderResultResponse.builder()
                    .orderId(savedOrder.getId())
                    .status(savedOrder.getStatus())
                    .message("Emir gönderim işlemi başarıyla tamamlanmıştır")
                    .submittedAt(savedOrder.getSubmittedAt())
                    .success(true)
                    .build();

        } catch (Exception e) {
            order.setStatus(OrderStatus.REJECTED);
            tradeOrderRepository.save(order);

            return StockSellOrderResultResponse.builder()
                    .orderId(order.getId())
                    .status(order.getStatus())
                    .message("Emir gönderim işleminiz başarısız olmuştur, lütfen tekrar deneyiniz")
                    .submittedAt(order.getSubmittedAt())
                    .success(false)
                    .build();
        }
    }

    private void validateOrderExecution(TradeOrder order) {
        // Borsa açık mı kontrolü
        if (!isBorsaOpen()) {
            throw new RuntimeException("Borsa kapalı");
        }

        // Hisse senedi işlem görüyor mu kontrolü
        Stock stock = order.getStock();
        if (!stock.getIsActive()) {
            throw new RuntimeException("Hisse senedi işlem görmüyor");
        }
    }

    private void updatePortfolio(TradeOrder order) {
        PortfolioItem portfolioItem = portfolioItemRepository
                .findByClientIdAndStockId(order.getClient().getId(), order.getStock().getId())
                .orElseThrow(() -> new RuntimeException("Portföy kalemi bulunamadı"));

        int newQuantity = portfolioItem.getQuantity() - order.getQuantity();
        if (newQuantity < 0) {
            throw new RuntimeException("Yetersiz hisse senedi miktarı");
        }

        portfolioItem.setQuantity(newQuantity);
        portfolioItem.setLastUpdated(LocalDateTime.now());
        portfolioItemRepository.save(portfolioItem);
    }

    private boolean isBorsaOpen() {
        // Borsa açık/kapalı kontrolü implementasyonu
        // Şimdilik basit bir kontrol
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        return hour >= 10 && hour < 18; // 10:00-18:00 arası açık
    }

    private String generateOrderReference() {
        // Benzersiz sipariş referansı oluştur
        return "ORD-" + System.currentTimeMillis();
    }

    private ClientSearchResponse mapToClientSearchResponse(Client client) {
        List<AccountSummaryResponse> accounts = accountRepository.findByClientId(client.getId())
                .stream()
                .map(account -> AccountSummaryResponse.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .currency(account.getCurrency())
                        .balance(account.getBalance())
                        .accountType(account.getAccountType())
                        .isPrimaryTakas(account.isPrimaryTakas())
                        .build())
                .collect(Collectors.toList());

        return ClientSearchResponse.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .tckn(client.getTckn())
                .vergiNo(client.getVergiNo())
                .blueCardNo(client.getBlueCardNo())
                .email(client.getEmail())
                .phone(client.getPhone())
                .status(client.getStatus())
                .clientType(client.getClientType())
                .createdAt(client.getCreatedAt())
                .accounts(accounts)
                .build();
    }

    private ClientStockHoldingResponse mapToClientStockHoldingResponse(PortfolioItem item) {
        return ClientStockHoldingResponse.builder()
                .stockId(item.getStock().getId())
                .stockSymbol(item.getStock().getSymbol())
                .stockName(item.getStock().getName())
                .stockGroup(item.getStock().getGroup())
                .availableQuantity(item.getQuantity())
                .currentPrice(item.getStock().getCurrentPrice())
                .avgPrice(item.getAvgPrice())
                .totalValue(item.getStock().getCurrentPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}
