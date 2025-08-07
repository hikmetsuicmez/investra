package com.investra.service;

import com.investra.entity.Stock;

import java.util.*;

public interface StockService {

    List<Stock> getAllStocks();
    Optional<Stock> getStockByCode(String stockCode);
    void updateStockPrice(Stock stock);
    List<Stock> refreshStocksFromApi();
}
