package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.enums.ClientType;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderType;
import com.investra.exception.AccountNotFoundException;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.InsufficientStockException;
import com.investra.exception.StockNotFoundException;
import com.investra.mapper.ClientMapper;
import com.investra.mapper.StockMapper;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.Optional;

import static com.investra.mapper.StockMapper.mapToStockSellOrderPreviewResponse;

@Service
@RequiredArgsConstructor
public class StockSellServiceImpl implements StockSellService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;

    private static final BigDecimal INDIVIDUAL_COMMISION_RATE = new BigDecimal("0.002"); // %0.2
    private static final BigDecimal CORPORATE_COMMISION_RATE = new BigDecimal("0.001"); // %0.1
    private static final BigDecimal BSMV_FEE_RATE = new BigDecimal("0.05");  // %5
    private final RestClient.Builder builder;

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
    public Response<StockSellOrderResultResponse> executeSellOrder(StockSellOrderRequest request) {
        return null;
    }
}
