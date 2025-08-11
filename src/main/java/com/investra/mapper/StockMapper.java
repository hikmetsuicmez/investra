package com.investra.mapper;

import com.investra.dtos.response.ClientStockHoldingResponse;
import com.investra.dtos.response.StockResponse;
import com.investra.dtos.response.StockSellOrderPreviewResponse;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderType;
import com.investra.enums.StockGroup;

import java.math.BigDecimal;
import java.time.LocalDate;

public class StockMapper {

    public static ClientStockHoldingResponse mapToClientStockHoldingResponse(PortfolioItem portfolioItem) {
        return ClientStockHoldingResponse.builder()
                .stockId(portfolioItem.getStock().getId())
                .stockName(portfolioItem.getStock().getName())
                .stockGroup(portfolioItem.getStock().getGroup())
                .availableQuantity(portfolioItem.getQuantity())
                .avgPrice(portfolioItem.getAvgPrice())
                .currentPrice(portfolioItem.getStock().getPrice())
                .category(portfolioItem.getStock().getCategory())
                .build();
    }

    public static StockSellOrderPreviewResponse mapToStockSellOrderPreviewResponse(
            String accountNumber,
            String stockName,
            String stockCode,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount,
            BigDecimal commission,
            BigDecimal bsmv,
            BigDecimal totalTaxAndCommission,
            BigDecimal netAmount,
            ExecutionType executionType,
            StockGroup stockGroup,
            OrderType orderType,
            LocalDate tradeDate,
            String valueDate) {
        return StockSellOrderPreviewResponse.builder()
                .accountNumber(accountNumber)
                .stockName(stockName)
                .stockSymbol(stockCode)
                .price(price)
                .quantity(quantity)
                .totalAmount(totalAmount)
                .commission(commission)
                .bsmv(bsmv)
                .totalTaxAndCommission(totalTaxAndCommission)
                .netAmount(netAmount)
                .executionType(executionType)
                .stockGroup(stockGroup)
                .orderType(orderType)
                .tradeDate(tradeDate)
                .valueDate(valueDate)
                .build();
    }

    public static StockResponse toStockResponse(Stock stock) {

        return StockResponse.builder()
                .id(stock.getId())
                .name(stock.getName())
                .symbol(stock.getCode())
                .currentPrice(stock.getPrice())
                .stockGroup(stock.getGroup().name())
                .isActive(stock.getIsActive())
                .category(stock.getCategory())
                .build();
    }
}
