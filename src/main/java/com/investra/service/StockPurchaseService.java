package com.investra.service;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.dtos.response.StockPurchaseOrderPreviewResponse;
import com.investra.dtos.response.StockPurchaseOrderResultResponse;

public interface StockPurchaseService {

    Response<StockOrderPreviewResponse> previewPurchaseOrder(StockOrderRequest request);
    Response<StockPurchaseOrderResultResponse> executePurchaseOrder(StockOrderRequest request, String userEmail);
}
