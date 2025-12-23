package com.example.high_five.service;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.dto.order.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway-server", url = "${gateway.uri}", contextId = "orderClient")
public interface OrderClient {

    @GetMapping("/api/orders/wrappers")
    List<OrderResponse.WrapperDto> getWrappers();

    @PostMapping("/api/orders")
    OrderCreateResponse createOrder(@RequestHeader("X-USER-ID") Long userId,
                                    @RequestBody OrderCheckoutRequest request);

    @GetMapping("/api/orders/policy/current")
    DeliveryPolicyResponse getDeliveryPolicy();

    @PostMapping("/api/orders")
    OrderCreateResponse createOrder(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-GUEST-ID", required = false) String guestId,
            @RequestBody OrderCheckoutRequest request
    );

    @Getter
    @NoArgsConstructor
    public static class OrderCreateResponse {
        private Long orderId;
        private String orderKey;
        private Integer totalAmount;
    }

    @PostMapping("/api/orders/{orderId}/cancel")
    void cancelOrder(@PathVariable("orderId") Long orderId);

    @PostMapping("/api/orders/{orderId}/confirm")
    void confirmOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/orders")
    CommonPageResponse<MyOrderResponse> getMyOrders(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    @GetMapping("/api/admin/orders")
    CommonPageResponse<OrderResponse> getAdminOrders(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status
    );

    @PutMapping("/api/admin/orders/{orderId}/status")
    void updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody OrderStatusUpdateRequest request
    );

    @PostMapping("/api/orders/{orderId}/returns")
    void requestReturn(@PathVariable("orderId") Long orderId, @RequestBody OrderReturnRequest request);

    // DTO 내부 클래스
    record OrderStatusUpdateRequest(String status, String trackingNumber) {}
}