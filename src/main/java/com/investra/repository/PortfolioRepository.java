package com.investra.repository;

import com.investra.entity.Client;
import com.investra.entity.Portfolio;
import com.investra.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long>{
    List<Portfolio> findAllByClientId(Long clientId);
    Optional<Portfolio> findByClientId(Long clientId);
    Optional<Portfolio> findByClient(Client client);
}
