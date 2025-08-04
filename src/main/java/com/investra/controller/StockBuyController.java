package com.investra.controller;

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
@RequestMapping("/api/stock/buy")
@RequiredArgsConstructor
@Slf4j
public class StockBuyController {

    private final StockBuyService stockBuyService;

    @PostMapping("/search-clients")
    public Response<List<ClientSearchResponse>> searchClients(@Valid @RequestBody ClientSearchRequest request) {
        log.info("Müşteri arama isteği alındı: {}", request);
        return stockBuyService.searchClients(request);
    }

    @GetMapping("/available-stocks")
    public Response<List<StockResponse>> getAvailableStocks() {
        log.info("Mevcut hisse senetleri isteği alındı");
        return stockBuyService.getAvailableStocks();
    }

    @PostMapping("/preview")
    public Response<StockBuyOrderPreviewResponse> previewBuyOrder(@Valid @RequestBody StockBuyOrderRequest request) {
        log.info("Alış önizleme isteği alındı: {}", request);
        return stockBuyService.previewBuyOrder(request);
    }

    @PostMapping("/execute")
    public Response<StockBuyOrderResultResponse> executeBuyOrder(
            @Valid @RequestBody StockBuyOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Alış işlemi isteği alındı: {}", request);
        return stockBuyService.executeBuyOrder(request, userDetails.getUsername());
    }
}
