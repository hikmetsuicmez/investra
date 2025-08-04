package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.*;
import com.investra.service.StockBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Stock.BASE + ApiEndpoints.Stock.BUY)
@RequiredArgsConstructor
@Slf4j
public class StockBuyController {

    private final StockBuyService stockBuyService;

    @PostMapping(ApiEndpoints.Stock.SEARCH_CLIENT)
    public Response<List<ClientSearchResponse>> searchClients(@Valid @RequestBody ClientSearchRequest request) {
        log.info("Müşteri arama isteği alındı: {}", request);
        return stockBuyService.searchClients(request);
    }

    @GetMapping(ApiEndpoints.Stock.AVAILABLE_STOCKS)
    public Response<List<StockResponse>> getAvailableStocks() {
        log.info("Mevcut hisse senetleri isteği alındı");
        return stockBuyService.getAvailableStocks();
    }

    @PostMapping(ApiEndpoints.Stock.PREVIEW_BUY_ORDER)
    public Response<StockBuyOrderPreviewResponse> previewBuyOrder(@Valid @RequestBody StockBuyOrderRequest request) {
        log.info("Alış önizleme isteği alındı: {}", request);
        return stockBuyService.previewBuyOrder(request);
    }

    @PostMapping(ApiEndpoints.Stock.EXECUTE_BUY_ORDER)
    public Response<StockBuyOrderResultResponse> executeBuyOrder(
            @Valid @RequestBody StockBuyOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Alış işlemi isteği alındı: {}", request);
        return stockBuyService.executeBuyOrder(request, userDetails.getUsername());
    }
}
