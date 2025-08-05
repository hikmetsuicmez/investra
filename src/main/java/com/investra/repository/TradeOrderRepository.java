package com.investra.repository;

import com.investra.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long>{

    List<TradeOrder> findAllByClientId(Long clientId);

}
