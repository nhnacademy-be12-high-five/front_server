package com.example.high_five.service;

import com.example.high_five.dto.order.DeliveryPolicyResponse;
import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
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
}