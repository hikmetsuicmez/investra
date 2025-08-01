package com.investra.controller;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.service.StockSellService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-sell")
@RequiredArgsConstructor
@Tag(name = "Hisse Senedi Satış İşlemleri", description = "Hisse senedi satış işlemleri için API endpoints")
public class StockSellController {

    private final StockSellService stockSellService;

    @PostMapping("/search-client")
    @Operation(summary = "Müşteri Arama",
               description = "Müşteri No/T.C NO/Vergi Kimlik NO/Müşteri İsmi/Hesap No/Mavi Kart No ile müşteri arama")
    public ResponseEntity<List<ClientSearchResponse>> searchClients(@RequestBody @Valid ClientSearchRequest request) {
        return ResponseEntity.ok(stockSellService.searchClients(request));
    }

    @GetMapping("/client/{clientId}/stocks")
    @Operation(summary = "Müşteri Hisse Senetleri",
               description = "Müşterinin sahip olduğu hisse senetlerini listeler")
    public ResponseEntity<List<ClientStockHoldingResponse>> getClientStockHoldings(@PathVariable Long clientId) {
        return ResponseEntity.ok(stockSellService.getClientStockHoldings(clientId));
    }

    @PostMapping("/preview")
    @Operation(summary = "Satış Emri Önizleme",
               description = "Hisse senedi satış emri için önizleme bilgilerini getirir")
    public ResponseEntity<StockSellOrderPreviewResponse> previewSellOrder(@RequestBody @Valid StockSellOrderRequest request) {
        return ResponseEntity.ok(stockSellService.previewSellOrder(request));
    }

    @PostMapping("/execute")
    @Operation(summary = "Satış Emri Gönder",
               description = "Hisse senedi satış emrini gönderir")
    public ResponseEntity<StockSellOrderResultResponse> executeSellOrder(@RequestBody @Valid StockSellOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Bu email adresini verecek
        return ResponseEntity.ok(stockSellService.executeSellOrder(request, username));
    }
}
