package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.Response;
import java.util.List;

public interface StockTradeService {

    Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request);
}
