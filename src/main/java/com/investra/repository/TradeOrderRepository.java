package com.investra.repository;

import com.investra.entity.TradeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeOrderRepository extends JpaRepository<TradeOrder,Long>{
}
