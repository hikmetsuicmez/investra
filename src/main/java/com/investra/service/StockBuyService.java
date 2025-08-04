package com.investra.service;

import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockBuyOrderPreviewResponse;
import com.investra.dtos.response.StockBuyOrderResultResponse;
import com.investra.dtos.response.StockResponse;

import java.util.List;


public interface StockBuyService extends StockTradeService {


    Response<List<StockResponse>> getAvailableStocks();

    Response<StockBuyOrderPreviewResponse> previewBuyOrder(StockBuyOrderRequest request);

    Response<StockBuyOrderResultResponse> executeBuyOrder(StockBuyOrderRequest request, String userEmail);
}
