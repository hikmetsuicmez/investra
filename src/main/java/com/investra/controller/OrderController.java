package com.investra.controller;

import com.investra.dtos.OrderCreateDto;
import com.investra.dtos.OrderCreateRequest;
import com.investra.dtos.OrderDTO;
import com.investra.dtos.OrderUpdateDto;
import com.investra.enums.OrderStatus;
import com.investra.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody OrderCreateDto orderCreateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO createdOrder = orderService.createOrder(orderCreateDto, userDetails.getUsername());
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getAllOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> pendingOrders = orderService.getPendingOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(pendingOrders);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<OrderDTO>> getCompletedOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> completedOrders = orderService.getCompletedOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(completedOrders);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<List<OrderDTO>> getCancelledOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> cancelledOrders = orderService.getCancelledOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(cancelledOrders);
    }

    @GetMapping("/partially-completed")
    public ResponseEntity<List<OrderDTO>> getPartiallyCompletedOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> partiallyCompletedOrders = orderService.getPartiallyCompletedOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(partiallyCompletedOrders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getOrdersByStatusAndUser(userDetails.getUsername(), status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateDto orderUpdateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO updatedOrder = orderService.updateOrder(orderId, orderUpdateDto, userDetails.getUsername());
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderDTO cancelledOrder = orderService.cancelOrder(orderId, userDetails.getUsername());
        return ResponseEntity.ok(cancelledOrder);
    }
}
