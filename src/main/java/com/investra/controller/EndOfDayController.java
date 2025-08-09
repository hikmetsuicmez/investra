package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.EndOfDayApiDocs;
import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.service.EndOfDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.investra.exception.BusinessException;
import com.investra.entity.Client;
import com.investra.entity.PortfolioItem;
import com.investra.entity.PortfolioDailyValuation;
import com.investra.entity.TradeOrder;
import com.investra.repository.ClientRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.PortfolioDailyValuationRepository;
import com.investra.repository.TradeOrderRepository;

@RestController
@RequestMapping(ApiEndpoints.EndOfDay.BASE)
@RequiredArgsConstructor
@Slf4j
public class EndOfDayController implements EndOfDayApiDocs {

    private final EndOfDayService endOfDayService;
    private final ClientRepository clientRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final PortfolioDailyValuationRepository portfolioDailyValuationRepository;

    @GetMapping(ApiEndpoints.EndOfDay.STATUS)
    public Response<EndOfDayStatusResponse> getEndOfDayStatus() {
        log.info("Gün sonu durumu sorgulanıyor");
        EndOfDayStatusResponse status = endOfDayService.getEndOfDayStatus();

        return Response.<EndOfDayStatusResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Gün sonu durumu başarıyla alındı")
                .data(status)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.FETCH_PRICES)
    public Response<Boolean> fetchLatestClosingPrices() {
        log.info("Kapanış fiyatları alınıyor");
        boolean success = endOfDayService.fetchLatestClosingPrices();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Kapanış fiyatları başarıyla alındı" : "Kapanış fiyatları alınamadı")
                .data(success)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.START_VALUATION)
    public Response<Boolean> startEndOfDayValuation(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Gün sonu değerleme başlatılıyor");
        endOfDayService.runEndOfDayValuation(userDetails.getUsername());

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Gün sonu değerleme tamamlandı")
                .data(true)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.PROCESS_T0_TO_T1)
    public Response<Boolean> processT0ToT1Settlement() {
        log.info("T+0 işlemleri T+1'e geçiriliyor");
        endOfDayService.processT0ToT1Settlement();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("T+0 işlemleri T+1'e geçirildi")
                .data(true)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.PROCESS_T1_TO_T2)
    public Response<Boolean> processT1ToT2Settlement() {
        log.info("T+1 işlemleri T+2'ye geçiriliyor");
        endOfDayService.processT1Settlement();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("T+1 işlemleri T+2'ye geçirildi")
                .data(true)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.PROCESS_T2_COMPLETION)
    public Response<Boolean> processT2Completion() {
        log.info("T+2 işlemleri tamamlanıyor");
        endOfDayService.processT2Settlement();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("T+2 işlemleri tamamlandı")
                .data(true)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.PROCESS_ALL_T2_STEPS)
    public Response<Boolean> processAllT2SettlementSteps() {
        log.info("Tüm T+2 settlement adımları başlatılıyor");
        endOfDayService.processAllT2SettlementSteps();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Tüm T+2 settlement adımları tamamlandı")
                .data(true)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.CLIENT_VALUATIONS)
    public Response<List<ClientValuationResponse>> getClientValuations() {
        log.info("Tüm müşteri değerlemeleri alınıyor");
        List<ClientValuationResponse> valuations = endOfDayService.getAllClientValuations();

        return Response.<List<ClientValuationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri değerlemeleri başarıyla alındı")
                .data(valuations)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.CLIENT_VALUATION)
    public Response<ClientValuationResponse> getClientValuation(@PathVariable Long clientId) {
        log.info("Müşteri değerlemesi alınıyor: {}", clientId);
        ClientValuationResponse valuation = endOfDayService.getClientValuation(clientId);

        return Response.<ClientValuationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri değerlemesi başarıyla alındı")
                .data(valuation)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.STOCK_PRICES)
    public Response<List<StockPriceResponse>> getStockPrices(
            @RequestParam(required = false) String date) {

        LocalDate priceDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        log.info("Hisse fiyatları alınıyor: {}", priceDate);

        List<StockPriceResponse> prices = endOfDayService.getAllStockPrices(priceDate);

        return Response.<List<StockPriceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Hisse fiyatları başarıyla alındı")
                .data(prices)
                .build();
    }

    // Debug endpoint'leri
    @GetMapping("/debug/client/{clientId}/trades")
    public Response<List<Object>> getClientTrades(@PathVariable Long clientId) {
        log.info("Debug: Müşteri işlemleri sorgulanıyor: {}", clientId);

        // Müşteri bilgilerini getir
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı"));

        // Tüm işlemleri getir
        List<TradeOrder> allTrades = tradeOrderRepository.findByClient(client);
        List<Object> tradeInfo = new ArrayList<>();

        for (TradeOrder trade : allTrades) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", trade.getId());
            info.put("stockCode", trade.getStock().getCode());
            info.put("quantity", trade.getQuantity());
            info.put("price", trade.getPrice());
            info.put("status", trade.getStatus());
            info.put("settlementStatus", trade.getSettlementStatus());
            info.put("orderType", trade.getOrderType());
            info.put("submittedAt", trade.getSubmittedAt());
            tradeInfo.add(info);
        }

        return Response.<List<Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri işlemleri başarıyla alındı")
                .data(tradeInfo)
                .build();
    }

    @GetMapping("/debug/client/{clientId}/portfolio")
    public Response<List<Object>> getClientPortfolio(@PathVariable Long clientId) {
        log.info("Debug: Müşteri portföyü sorgulanıyor: {}", clientId);

        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(clientId);
        List<Object> portfolioInfo = new ArrayList<>();

        for (PortfolioItem item : portfolioItems) {
            Map<String, Object> info = new HashMap<>();
            info.put("stockCode", item.getStock().getCode());
            info.put("quantity", item.getQuantity());
            info.put("avgPrice", item.getAvgPrice());
            portfolioInfo.add(info);
        }

        return Response.<List<Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri portföyü başarıyla alındı")
                .data(portfolioInfo)
                .build();
    }

    @GetMapping("/debug/valuations/{date}")
    public Response<List<Object>> getValuationsByDate(@PathVariable String date) {
        log.info("Debug: Değerlemeler sorgulanıyor: {}", date);

        LocalDate valuationDate = LocalDate.parse(date);
        List<PortfolioDailyValuation> valuations = portfolioDailyValuationRepository
                .findAllByValuationDate(valuationDate);

        List<Object> valuationInfo = new ArrayList<>();
        for (PortfolioDailyValuation valuation : valuations) {
            Map<String, Object> info = new HashMap<>();
            info.put("clientId", valuation.getClient().getId());
            info.put("clientName", valuation.getClient().getFullName());
            info.put("totalValue", valuation.getTotalPortfolioValue());
            info.put("unrealizedPL", valuation.getUnrealizedProfitLoss());
            info.put("valuationDate", valuation.getValuationDate());
            valuationInfo.add(info);
        }

        return Response.<List<Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Değerlemeler başarıyla alındı")
                .data(valuationInfo)
                .build();
    }

    @GetMapping("/debug/client/{clientId}/settlement-status")
    public Response<List<Object>> getClientSettlementStatus(@PathVariable Long clientId) {
        log.info("Debug: Müşteri settlement status'u sorgulanıyor: {}", clientId);

        // Müşteri bilgilerini getir
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("Müşteri bulunamadı"));

        // Tüm işlemleri getir
        List<TradeOrder> allTrades = tradeOrderRepository.findByClient(client);
        List<Object> settlementInfo = new ArrayList<>();

        for (TradeOrder trade : allTrades) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", trade.getId());
            info.put("stockCode", trade.getStock().getCode());
            info.put("quantity", trade.getQuantity());
            info.put("price", trade.getPrice());
            info.put("status", trade.getStatus());
            info.put("settlementStatus", trade.getSettlementStatus());
            info.put("orderType", trade.getOrderType());
            info.put("submittedAt", trade.getSubmittedAt());
            info.put("settledAt", trade.getSettledAt());
            info.put("settlementDaysRemaining", trade.getSettlementDaysRemaining());
            settlementInfo.add(info);
        }

        return Response.<List<Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri settlement status'u başarıyla alındı")
                .data(settlementInfo)
                .build();
    }

    @GetMapping("/debug/clients-with-no-activity")
    public Response<List<Object>> getClientsWithNoActivity() {
        log.info("Debug: İşlemi olmayan müşteriler sorgulanıyor");

        // Tüm aktif müşterileri getir
        List<Client> allClients = clientRepository.findAllByIsActive(true);
        List<Object> clientsWithNoActivity = new ArrayList<>();

        for (Client client : allClients) {
            // Portföy pozisyonlarını kontrol et
            List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(client.getId());

            // EXECUTED işlemleri kontrol et
            List<TradeOrder> executedTrades = tradeOrderRepository.findByClientAndStatus(
                    client, com.investra.enums.OrderStatus.EXECUTED);

            // Eğer hiç işlemi yoksa listeye ekle
            if (portfolioItems.isEmpty() && executedTrades.isEmpty()) {
                Map<String, Object> clientInfo = new HashMap<>();
                clientInfo.put("clientId", client.getId());
                clientInfo.put("clientName", client.getFullName());
                clientInfo.put("portfolioItemsCount", portfolioItems.size());
                clientInfo.put("executedTradesCount", executedTrades.size());
                clientsWithNoActivity.add(clientInfo);
            }
        }

        return Response.<List<Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("İşlemi olmayan müşteriler başarıyla alındı")
                .data(clientsWithNoActivity)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.MANUALLY_UPDATE_PRICES)
    public Response<Boolean> manuallyUpdateClosingPrices() {
        log.info("Kapanış fiyatları manuel olarak güncelleniyor");
        boolean success = endOfDayService.manuallyUpdateClosingPrices();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Gün sonu fiyatları başarıyla güncellendi" : "Gün sonu fiyatları güncellenemedi")
                .data(success)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.RESET_STATUS)
    public Response<Boolean> resetEndOfDayStatus() {
        log.info("Test amaçlı gün sonu durumu sıfırlanıyor");
        boolean success = endOfDayService.resetEndOfDayStatus();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Gün sonu durumu başarıyla sıfırlandı" : "Gün sonu durumu sıfırlanamadı")
                .data(success)
                .build();
    }
}
