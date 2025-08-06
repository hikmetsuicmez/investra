package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.TradeOrderApiDocs;
import com.investra.dtos.response.Response;
import com.investra.entity.TradeOrder;
import com.investra.enums.OrderStatus;
import com.investra.enums.SettlementStatus;
import com.investra.service.TradeOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.TradeOrder.BASE)
@RequiredArgsConstructor
public class TradeOrderController implements TradeOrderApiDocs {

    private final TradeOrderService tradeOrderService;

    @GetMapping(ApiEndpoints.TradeOrder.GET_ALL)
    public ResponseEntity<Response<List<TradeOrder>>> getAllOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<TradeOrder> orders = tradeOrderService.getAllOrdersByUser(userDetails.getUsername());

        return ResponseEntity.ok(
                Response.<List<TradeOrder>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Emirler başarıyla listelendi")
                .data(orders)
                .build()
        );
    }

    @GetMapping(ApiEndpoints.TradeOrder.GET_PENDING)
    public ResponseEntity<Response<List<TradeOrder>>> getPendingOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<TradeOrder> pendingOrders = tradeOrderService.getOrdersByStatusAndUser(
                userDetails.getUsername(), OrderStatus.PENDING);

        return ResponseEntity.ok(
                Response.<List<TradeOrder>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Bekleyen emirler başarıyla listelendi")
                .data(pendingOrders)
                .build()
        );
    }

    @GetMapping(ApiEndpoints.TradeOrder.GET_EXECUTED)
    public ResponseEntity<Response<List<TradeOrder>>> getExecutedOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<TradeOrder> executedOrders = tradeOrderService.getOrdersByStatusAndUser(
                userDetails.getUsername(), OrderStatus.EXECUTED);

        return ResponseEntity.ok(
                Response.<List<TradeOrder>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Gerçekleşen emirler başarıyla listelendi")
                .data(executedOrders)
                .build()
        );
    }

    @GetMapping(ApiEndpoints.TradeOrder.GET_COMPLETED)
    public ResponseEntity<Response<List<TradeOrder>>> getSettledOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<TradeOrder> settledOrders = tradeOrderService.getAllOrdersByUser(userDetails.getUsername()).stream()
                .filter(order -> order.getStatus() == OrderStatus.EXECUTED)
                .filter(order -> order.getSettlementStatus() == SettlementStatus.COMPLETED)
                .toList();

        return ResponseEntity.ok(
                Response.<List<TradeOrder>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Takas tamamlanan emirler başarıyla listelendi")
                .data(settledOrders)
                .build()
        );
    }

    @GetMapping(ApiEndpoints.TradeOrder.GET_CANCELLED)
    public ResponseEntity<Response<List<TradeOrder>>> getCancelledOrders(@AuthenticationPrincipal UserDetails userDetails) {
        List<TradeOrder> cancelledOrders = tradeOrderService.getOrdersByStatusAndUser(
                userDetails.getUsername(), OrderStatus.CANCELLED);

        return ResponseEntity.ok(
                Response.<List<TradeOrder>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("İptal edilen emirler başarıyla listelendi")
                .data(cancelledOrders)
                .build()
        );
    }

    @PostMapping(ApiEndpoints.TradeOrder.ORDER_CANCELLED)
    public ResponseEntity<Response<TradeOrder>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        TradeOrder cancelledOrder = tradeOrderService.cancelOrder(orderId, userDetails.getUsername());

        return ResponseEntity.ok(
                Response.<TradeOrder>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Emir başarıyla iptal edildi")
                .data(cancelledOrder)
                .build()
        );
    }
}
