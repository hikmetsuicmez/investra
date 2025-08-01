package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.service.StockSellService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Stock.BASE + ApiEndpoints.Stock.SELL)
@RequiredArgsConstructor
public class StockSellController {

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
    public ResponseEntity<Response<StockSellOrderPreviewResponse>> previewSellOrder(@RequestBody @Valid StockSellOrderRequest request) {
        return ResponseEntity.ok(stockSellService.previewSellOrder(request));
    }

    @PostMapping("/execute")
    public ResponseEntity<Response<StockSellOrderResultResponse>> executeSellOrder(@RequestBody @Valid StockSellOrderRequest request) {
        return ResponseEntity.ok(stockSellService.executeSellOrder(request));
    }

}
