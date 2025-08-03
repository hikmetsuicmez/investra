package com.investra.mapper;

import com.investra.dtos.response.ClientStockHoldingResponse;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.entity.PortfolioItem;
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
                .stockSymbol(portfolioItem.getStock().getSymbol())
                .stockGroup(portfolioItem.getStock().getGroup())
                .availableQuantity(portfolioItem.getQuantity())
                .avgPrice(portfolioItem.getAvgPrice())
                .currentPrice(portfolioItem.getStock().getCurrentPrice())
                .build();
    }

    public static StockOrderPreviewResponse mapToStockSellOrderPreviewResponse(
            String accountNumber,
            String stockName,
            String stockSymbol,
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
        return StockOrderPreviewResponse.builder()
                .accountNumber(accountNumber)
                .stockName(stockName)
                .stockSymbol(stockSymbol)
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

}
