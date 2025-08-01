package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import jakarta.validation.Valid;

import java.util.List;

public interface StockSellService {
    Response<List<ClientSearchResponse>> searchClients(@Valid ClientSearchRequest request);
    Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId);
    Response<StockSellOrderPreviewResponse> previewSellOrder(@Valid StockSellOrderRequest request);
    Response<StockSellOrderResultResponse> executeSellOrder(@Valid StockSellOrderRequest request);
}
