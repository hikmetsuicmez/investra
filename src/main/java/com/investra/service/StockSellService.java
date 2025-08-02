package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;

import java.util.List;

public interface StockSellService {
    Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request);
    Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId);
    Response<StockSellOrderPreviewResponse> previewSellOrder(StockSellOrderRequest request);
    Response<StockSellOrderResultResponse> executeSellOrder(StockSellOrderRequest request, String userEmail);
}
