package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.dtos.response.StockPurchaseOrderResultResponse;
import com.investra.service.StockPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Stock.BASE + ApiEndpoints.Stock.PURCHASE)
@RequiredArgsConstructor
public class StockPurchaseController {

    private final StockPurchaseService stockPurchaseService;

    @PostMapping(ApiEndpoints.Stock.PREVIEW_PURCHASE_ORDER)
    public ResponseEntity<Response<StockOrderPreviewResponse>> previewPurchaseOrder(StockOrderRequest request) {
        return ResponseEntity.ok(stockPurchaseService.previewPurchaseOrder(request));
    }

    @PostMapping(ApiEndpoints.Stock.EXECUTE_PURCHASE_ORDER)
    ResponseEntity<Response<StockPurchaseOrderResultResponse>> executePurchaseOrder(StockOrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return ResponseEntity.ok(stockPurchaseService.executePurchaseOrder(request,userEmail));
    }

}