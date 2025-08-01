package com.investra.repository;

import com.investra.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends  JpaRepository<Portfolio,Long>{
}
