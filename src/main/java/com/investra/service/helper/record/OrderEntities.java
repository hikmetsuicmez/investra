package com.investra.service.helper.record;

import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;

public record OrderEntities(Client client, Stock stock, PortfolioItem portfolioItem, Account account) {}
