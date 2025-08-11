package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.TradeOrderApiDocs;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.TradeOrderDTO;
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
        public ResponseEntity<Response<List<TradeOrderDTO>>> getAllOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> orders = tradeOrderService.getAllOrdersByUser(userDetails.getUsername());

                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("Emirler başarıyla listelendi")
                                                .data(orders)
                                                .build());
        }

        @GetMapping(ApiEndpoints.TradeOrder.GET_PENDING)
        public ResponseEntity<Response<List<TradeOrderDTO>>> getPendingOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> pendingOrders = tradeOrderService.getOrdersByStatusAndUser(
                                userDetails.getUsername(), OrderStatus.PENDING);

                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("Bekleyen emirler başarıyla listelendi")
                                                .data(pendingOrders)
                                                .build());
        }

        @GetMapping(ApiEndpoints.TradeOrder.GET_EXECUTED)
        public ResponseEntity<Response<List<TradeOrderDTO>>> getExecutedOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> executedOrders = tradeOrderService.getOrdersByStatusAndUser(
                                userDetails.getUsername(), OrderStatus.EXECUTED);

                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("Gerçekleşen emirler başarıyla listelendi")
                                                .data(executedOrders)
                                                .build());
        }

        @GetMapping(ApiEndpoints.TradeOrder.GET_COMPLETED)
        public ResponseEntity<Response<List<TradeOrderDTO>>> getSettledOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> settledOrders = tradeOrderService.getAllOrdersByUser(userDetails.getUsername())
                                .stream()
                                .filter(order -> order.getSettlementStatus() == SettlementStatus.COMPLETED)
                                .toList();
                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("Takas tamamlanan emirler başarıyla listelendi")
                                                .data(settledOrders)
                                                .build());
        }

        @GetMapping(ApiEndpoints.TradeOrder.GET_CANCELLED)
        public ResponseEntity<Response<List<TradeOrderDTO>>> getCancelledOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> cancelledOrders = tradeOrderService.getOrdersByStatusAndUser(
                                userDetails.getUsername(), OrderStatus.CANCELLED);
                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("İptal edilen emirler başarıyla listelendi")
                                                .data(cancelledOrders)
                                                .build());
        }

        @GetMapping(ApiEndpoints.TradeOrder.GET_CANCELLABLE)
        public ResponseEntity<Response<List<TradeOrderDTO>>> getCancellableOrders(
                        @AuthenticationPrincipal UserDetails userDetails) {
                List<TradeOrderDTO> cancellableOrders = tradeOrderService
                                .getCancellableOrdersByUser(userDetails.getUsername());
                return ResponseEntity.ok(
                                Response.<List<TradeOrderDTO>>builder()
                                                .statusCode(HttpStatus.OK.value())
                                                .message("İptal edilebilir emirler başarıyla listelendi")
                                                .data(cancellableOrders)
                                                .build());
        }

        @PostMapping(ApiEndpoints.TradeOrder.ORDER_CANCELLED)
        public ResponseEntity<Response<TradeOrderDTO>> cancelOrder(
                        @RequestParam Long orderId,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Response<TradeOrderDTO> response = tradeOrderService.cancelOrder(orderId, userDetails.getUsername());
                return ResponseEntity.ok(response);
        }
}
