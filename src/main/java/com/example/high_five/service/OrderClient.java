package com.example.high_five.service;

import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "gateway-server", url = "${gateway.uri}", contextId = "orderClient")
public interface OrderClient {

    @GetMapping("/api/orders/wrappers")
    List<OrderResponse.WrapperDto> getWrappers();

    // [수정] userId 헤더 추가 및 반환 타입 구체화
    @PostMapping("/api/orders")
    OrderCreateResponse createOrder(@RequestHeader("X-USER-ID") Long userId,
                                    @RequestBody OrderCheckoutRequest request);

    @Getter
    @NoArgsConstructor
    public static class OrderCreateResponse {
        private Long orderId;
        private String orderKey;
        private Integer totalAmount;
    }
}