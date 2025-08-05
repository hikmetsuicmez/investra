package com.investra.repository;

import com.investra.entity.Order;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

    List<Order> findByUserAndStatusInOrderByCreatedAtDesc(User user, List<OrderStatus> statuses);
}
