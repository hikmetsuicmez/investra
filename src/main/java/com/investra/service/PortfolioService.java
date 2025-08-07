package com.investra.service;

import com.investra.dtos.request.PortfolioCreateRequest;
import com.investra.dtos.response.PortfolioDTO;
import com.investra.dtos.response.Response;
import com.investra.entity.*;
import com.investra.enums.OrderType;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {

    Response<PortfolioDTO> createPortfolio(PortfolioCreateRequest request);
    void updatePortfolioAfterSettlement(TradeOrder order);
    void updatePortfolioWithEntities(
            Long orderId,
            OrderType orderType,
            Integer quantity,
            BigDecimal price,
            Stock stock,
            Account account,
            Client client);
    Response<List<PortfolioDTO>> getAllPortfolio();
    Response<PortfolioDTO> getPortfolioByClientId(Long clientId);
    Response<Void> deletePortfolioByClientId(Long clientId);

}

