package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.ClientType;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderStatus;
import com.investra.enums.OrderType;
import com.investra.exception.*;
import com.investra.mapper.ClientMapper;
import com.investra.mapper.StockMapper;
import com.investra.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Optional;

import static com.investra.mapper.StockMapper.mapToStockSellOrderPreviewResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockSellServiceImpl implements StockSellService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    private static final BigDecimal INDIVIDUAL_COMMISION_RATE = new BigDecimal("0.002"); // %0.2
    private static final BigDecimal CORPORATE_COMMISION_RATE = new BigDecimal("0.001"); // %0.1
    private static final BigDecimal BSMV_FEE_RATE = new BigDecimal("0.05");  // %5
    private final RestClient.Builder builder;
    private final UserRepository userRepository;
    private final TradeOrderRepository tradeOrderRepository;

    @Override
    public Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request) {
        var strategy = getStringOptionalFunction(request);
        List<Client> clients = strategy != null
                ? strategy.apply(request.getSearchTerm()).map(List::of).orElse(List.of())
                : List.of();

        List<ClientSearchResponse> responseClients = clients.stream()
                .map(ClientMapper::mapToClientSearchResponse)
                .toList();

        return Response.<List<ClientSearchResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri bulundu")
                .data(responseClients)
                .build();
    }

    private Function<String, Optional<Client>> getStringOptionalFunction(ClientSearchRequest request) {
        var searchStrategies = Map.of(
                "TCKN", clientRepository::findByTckn,
                "VERGI_N0", clientRepository::findByVergiNo,
                "MAVI_KART_NO", clientRepository::findByBlueCardNo,
                "NAME", (Function<String, Optional<Client>>) term ->
                        clientRepository.findAll().stream()
                                .filter(client -> client.getFullName().toLowerCase().contains(term.toLowerCase()))
                                .findFirst()

        );
        return searchStrategies.get(request.getSearchType());
    }

    @Override
    public Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());


        List<ClientStockHoldingResponse> stockHoldings = portfolioItems.stream()
                .map(StockMapper::mapToClientStockHoldingResponse)
                .toList();

        return Response.<List<ClientStockHoldingResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri bulundu")
                .data(stockHoldings)
                .build();
    }

    @Override
    @Transactional
    public Response<StockSellOrderPreviewResponse> previewSellOrder(StockSellOrderRequest request) {

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

        validateSellOrderRequest(request);

        BigDecimal commissionRate = client.getClientType() == ClientType.INDIVIDUAL
                ? INDIVIDUAL_COMMISION_RATE
                : CORPORATE_COMMISION_RATE;

        BigDecimal price = request.getExecutionType() == ExecutionType.MARKET
                ? stock.getCurrentPrice()
                : request.getPrice();

        BigDecimal commission = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .multiply(commissionRate);

        BigDecimal bsmv = commission.multiply(BSMV_FEE_RATE);

        BigDecimal totalTaxAndCommission = commission.add(bsmv);

        BigDecimal totalAmount = price
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        BigDecimal netAmount = totalAmount.subtract(totalTaxAndCommission);

        StockSellOrderPreviewResponse StockSellOrderPreviewResponse =
                mapToStockSellOrderPreviewResponse(
                        account.getAccountNumber(),
                        stock.getName(),
                        stock.getSymbol(),
                        price,
                        request.getQuantity(),
                        totalAmount,
                        commission,
                        bsmv,
                        totalTaxAndCommission,
                        netAmount,
                        request.getExecutionType(),
                        stock.getGroup(),
                        OrderType.SELL,
                        LocalDate.now(),
                        null
                );

        return Response.<StockSellOrderPreviewResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Satış önizleme başarılı")
                .data(StockSellOrderPreviewResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<StockSellOrderResultResponse> executeSellOrder(StockSellOrderRequest request, String userEmail) {
        if (!request.getPreviewConfirmed()) {
            throw new BadRequestException("Satış önizlemesi onaylanmadı. Lütfen önizlemeyi onaylayın.");
        }
        validateSellOrderRequest(request);

        StockSellOrderPreviewResponse previewResponse =
                previewSellOrder(request).getData();

        User submittedBy = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userEmail));
        log.info("Kullanıcı: {} tarafından satış işlemi başlatıldı.", submittedBy.getEmail());

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

        BigDecimal commissionRate = client.getClientType() == ClientType.INDIVIDUAL
                ? INDIVIDUAL_COMMISION_RATE
                : CORPORATE_COMMISION_RATE;

        BigDecimal price = request.getExecutionType() == ExecutionType.MARKET
                ? stock.getCurrentPrice()
                : request.getPrice();

        BigDecimal commission = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .multiply(commissionRate);

        BigDecimal bsmv = commission.multiply(BSMV_FEE_RATE);

        BigDecimal totalTaxAndCommission = commission.add(bsmv);

        BigDecimal totalAmount = price
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        BigDecimal netAmount = totalAmount.subtract(totalTaxAndCommission);

        TradeOrder order = TradeOrder.builder()
                .client(client)
                .account(account)
                .stock(stock)
                .orderType(OrderType.SELL)
                .executionType(request.getExecutionType())
                .price(price)
                .quantity(request.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .user(submittedBy)
                .submittedAt(LocalDateTime.now())
                .executedAt(LocalDate.now().plusDays(2).atStartOfDay())
                .build();

        try {
            validateOrderExecution(order);
            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(LocalDateTime.now());
            updatePortfolio(order);
            updateAccountBalance(account, netAmount);
            deletePortfolioItemIfZeroQuantity(portfolioItem);
            TradeOrder savedOrder = tradeOrderRepository.save(order);

            StockSellOrderResultResponse resultResponse = StockSellOrderResultResponse.builder()
                    .orderId(savedOrder.getId())
                    .orderStatus(savedOrder.getStatus())
                    .message("Satış işlemi başarıyla gerçekleştirildi.")
                    .submittedAt(savedOrder.getSubmittedAt())
                    .success(true)
                    .build();

            return Response.<StockSellOrderResultResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Satış işlemi başarılı")
                    .data(resultResponse)
                    .build();

        } catch (Exception e) {
            order.setStatus(OrderStatus.REJECTED);
            tradeOrderRepository.save(order);

            StockSellOrderResultResponse resultResponse = StockSellOrderResultResponse.builder()
                    .orderId(order.getId())
                    .orderStatus(order.getStatus())
                    .message("Satış işleminiz başarısız oldu.")
                    .submittedAt(order.getSubmittedAt())
                    .success(false)
                    .build();

            throw e;
        }
    }

    private void validateSellOrderRequest(StockSellOrderRequest request) {
        if (request.getExecutionType() == null)
            throw new BadRequestException("Execution type is required.");

        request.validatePrice();

        if (request.getQuantity() <= 0)
            throw new BadRequestException("Quantity is required.");

    }

    private void validateOrderExecution(TradeOrder order) {
        if (!isStockMarketOpen()) {
            throw new BadRequestException("Borsa şu anda kapalı. Lütfen borsa açıkken tekrar deneyin.");
        }

        Stock stock = order.getStock();
        if (!stock.getIsActive()) {
            throw new InactiveStockException("Bu hisse senedi aktif değil: " + stock.getName());
        }
    }

    private boolean isStockMarketOpen() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        return hour >=10 && hour < 18;
    }

    private void updatePortfolio(TradeOrder order) {

        PortfolioItem portfolioItem = portfolioItemRepository.findByClientIdAndStockId(
                order.getClient().getId(), order.getStock().getId())
                .orElseThrow(() -> new StockNotFoundException("Müşterinin portföyünde hisse senedi bulunamadı"));

        int newQuantity = portfolioItem.getQuantity() - order.getQuantity();

        portfolioItem.setQuantity(newQuantity);
        portfolioItem.setLastUpdated(LocalDateTime.now());
        portfolioItemRepository.save(portfolioItem);
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    // Eğer satılan quantity varlığımızda olan quantity ile eşitse portfolioitemdan STOCK SİLİNİR.??
    private void deletePortfolioItemIfZeroQuantity(PortfolioItem portfolioItem) {
        if (portfolioItem.getQuantity() <= 0) {
            portfolioItemRepository.delete(portfolioItem);
        }
    }

}
