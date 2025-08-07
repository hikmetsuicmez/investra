package com.investra.service;

import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.StockPriceResponse;

import java.time.LocalDate;
import java.util.List;

public interface EndOfDayService {

    // BİST kapanış fiyatlarını API'den çeker
    boolean fetchLatestClosingPrices();

    // Tüm müşteri portföylerini gün sonu kapanış fiyatlarıyla değerler
    void runEndOfDayValuation(String username);

    // Son değerleme tarihini ve durumunu getirir
    EndOfDayStatusResponse getEndOfDayStatus();

    // Belirli bir müşterinin gün sonu değerlemesini getirir
    ClientValuationResponse getClientValuation(Long clientId);

    // Tüm müşterilerin gün sonu değerlemelerini getirir
    List<ClientValuationResponse> getAllClientValuations();

    // Tüm hisse senetlerinin güncel fiyatlarını getirir
    List<StockPriceResponse> getAllStockPrices(LocalDate date);

    // Kapanış fiyatlarını manuel olarak günceller
    boolean manuallyUpdateClosingPrices();
}
