package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.Response;
import java.util.List;

/**
 * Hisse senedi işlemleri için genel arayüz
 * Bu arayüz, hem alış hem satış işlemleri için ortak metodları tanımlar
 */
public interface StockTradeService {
    /**
     * Müşteri arama işlemi
     * @param request Arama kriterleri
     * @return Bulunan müşteri listesi
     */
    Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request);
}
