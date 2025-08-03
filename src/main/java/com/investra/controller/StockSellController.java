package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.StockSellApiDocs;
import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.*;
import com.investra.service.StockSellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Stock.BASE + ApiEndpoints.Stock.SELL)
@RequiredArgsConstructor
public class StockSellController implements StockSellApiDocs {

    private final StockSellService stockSellService;

    @PostMapping(ApiEndpoints.Stock.SEARCH_CLIENT)
    public ResponseEntity<Response<List<ClientSearchResponse>>> searchClients(@RequestBody @Valid ClientSearchRequest request){
        return ResponseEntity.ok(stockSellService.searchClients(request));
    }

    @GetMapping(ApiEndpoints.Stock.CLIENT_STOCK_HOLDINGS)
    public ResponseEntity<Response<List<ClientStockHoldingResponse>>> getClientStockHoldings(@PathVariable Long clientId){
        return ResponseEntity.ok(stockSellService.getClientStockHoldings(clientId));
    }

    @PostMapping(ApiEndpoints.Stock.PREVIEW_SELL_ORDER)
    public ResponseEntity<Response<StockOrderPreviewResponse>> previewSellOrder(@RequestBody @Valid StockOrderRequest request) {
        return ResponseEntity.ok(stockSellService.previewSellOrder(request));
    }

    @PostMapping(ApiEndpoints.Stock.EXECUTE_SELL_ORDER)
    public ResponseEntity<Response<StockSellOrderResultResponse>> executeSellOrder(@RequestBody @Valid StockOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return ResponseEntity.ok(stockSellService.executeSellOrder(request,userEmail));
    }
}
