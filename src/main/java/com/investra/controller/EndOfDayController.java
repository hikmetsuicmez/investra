package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.EndOfDayApiDocs;
import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.SimulationStatusResponse;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.service.EndOfDayService;
import com.investra.service.SimulationDateService;
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
    private final SimulationDateService simulationDateService;
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

    // ESKİ ENDPOINT'LER KALDIRILDI - Şimdi sadece advance-full-day kullanılıyor

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

        LocalDate priceDate;
        if (date != null) {
            priceDate = LocalDate.parse(date);
        } else {
            priceDate = simulationDateService.getCurrentSimulationDate();
        }
        log.info("Hisse fiyatları alınıyor: {} (simülasyon tarihi: {})", priceDate,
                simulationDateService.getCurrentSimulationDate());

        List<StockPriceResponse> prices = endOfDayService.getAllStockPrices(priceDate);

        return Response.<List<StockPriceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Hisse senedi kapanış fiyatları başarıyla alındı")
                .data(prices)
                .build();
    }

    // Debug endpoint'leri
    @GetMapping(ApiEndpoints.EndOfDay.DEBUG_CLIENT_TRADES)
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

    @GetMapping(ApiEndpoints.EndOfDay.DEBUG_CLIENT_PORTFOLIO)
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

    @GetMapping(ApiEndpoints.EndOfDay.DEBUG_VALUATIONS_BY_DATE)
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

    @GetMapping(ApiEndpoints.EndOfDay.DEBUG_CLIENT_SETTLEMENT)
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

    @GetMapping(ApiEndpoints.EndOfDay.DEBUG_CLIENTS_NO_ACTIVITY)
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

    // RESET/MANUAL ENDPOINT'LER KALDIRILDI - reset-simulation kullan

    // Yeni Simülasyon Tabanlı Endpoint'ler

    @PostMapping(ApiEndpoints.EndOfDay.ADVANCE_FULL_DAY)
    public Response<Map<String, Object>> advanceFullDay(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Tam gün atlatma işlemi başlatılıyor, kullanıcı: {}", userDetails.getUsername());

        LocalDate oldDate = simulationDateService.getCurrentSimulationDate();
        boolean success = endOfDayService.advanceFullDay(userDetails.getUsername());
        LocalDate newDate = simulationDateService.getCurrentSimulationDate();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("oldDate", oldDate);
        responseData.put("newDate", newDate);
        responseData.put("daysAdvanced", simulationDateService.getDaysAdvanced());

        return Response.<Map<String, Object>>builder()
                .statusCode(HttpStatus.OK.value())
                .message(String.format("Gün sonu değerleme tamamlandı: %s", newDate))
                .data(responseData)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.SIMULATION_STATUS)
    public Response<SimulationStatusResponse> getSimulationStatus() {
        log.info("Simülasyon durumu sorgulanıyor");

        var simulationData = simulationDateService.getSimulationStatus();

        SimulationStatusResponse response = SimulationStatusResponse.builder()
                .currentSimulationDate(simulationData.getCurrentSimulationDate())
                .initialDate(simulationData.getInitialDate())
                .realDate(LocalDate.now())
                .daysAdvanced(simulationData.getDaysAdvanced())
                .lastUpdatedBy(simulationData.getUpdatedBy())
                .lastUpdatedAt(simulationData.getLastUpdatedAt())
                .description(simulationData.getDescription())
                .isAdvanced(simulationData.isAdvanced())
                .status(simulationData.isAdvanced() ? "SIMULATED" : "REAL_TIME")
                .message(simulationData.isAdvanced() ? "Sistem simülasyon modunda çalışıyor"
                        : "Sistem gerçek zamanda çalışıyor")
                .build();

        return Response.<SimulationStatusResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Simülasyon durumu başarıyla alındı")
                .data(response)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.RESET_SIMULATION)
    public Response<Boolean> resetSimulationDate(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Simülasyon tarihi sıfırlanıyor, kullanıcı: {}", userDetails.getUsername());
        boolean success = endOfDayService.resetSimulationDate(userDetails.getUsername());

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Simülasyon tarihi başarıyla sıfırlandı" : "Simülasyon tarihi sıfırlanamadı")
                .data(success)
                .build();
    }
}
