package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.*;

import java.util.List;

public interface StockSellService {
    Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request);
    Response<List<ClientStockHoldingResponse>> getClientStockHoldings(Long clientId);
    Response<StockOrderPreviewResponse> previewSellOrder(StockOrderRequest request);
    Response<StockSellOrderResultResponse> executeSellOrder(StockOrderRequest request, String userEmail);
}
