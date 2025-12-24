package com.example.high_five.service;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.dto.order.*; // 외부 DTO 패키지 임포트
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "gateway-server", url = "${gateway.uri}", contextId = "orderClient")
public interface OrderClient {

    // 1. 포장지 목록 조회
    @GetMapping("/api/orders/wrappers")
    List<OrderResponse.WrapperDto> getWrappers();

    // 2. 배송 정책 조회
    @GetMapping("/api/orders/policy/current")
    DeliveryPolicyResponse getDeliveryPolicy();

    // 3. 주문 생성 (회원/비회원 통합)
    @PostMapping("/api/orders")
    OrderCreateResponse createOrder(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-GUEST-ID", required = false) String guestId,
            @RequestBody OrderCheckoutRequest request
    );

    // 4. 주문 취소
    @PostMapping("/api/orders/{orderId}/cancel")
    void cancelOrder(@PathVariable("orderId") Long orderId);

    // 5. 주문 확정 (구매 확정)
    @PostMapping("/api/orders/{orderId}/confirm")
    void confirmOrder(@PathVariable("orderId") Long orderId);

    // 6. 마이페이지 주문 목록 조회
    @GetMapping("/api/orders")
    CommonPageResponse<MyOrderResponse> getMyOrders(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 7. [관리자] 주문 목록 조회
    @GetMapping("/api/admin/orders")
    CommonPageResponse<OrderResponse> getAdminOrders(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status
    );

    // 8. [관리자] 주문 상태 변경
    @PutMapping("/api/admin/orders/{orderId}/status")
    void updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody OrderStatusUpdateRequest request
    );

    // 9. [사용자] 반품 요청
    @PostMapping("/api/orders/{orderId}/returns")
    void requestReturn(@PathVariable("orderId") Long orderId, @RequestBody OrderReturnRequest request);

    // 10. [추가] 비회원 주문 조회
    // 백엔드의 @PostMapping("/guests/search")와 매핑됩니다.
    @PostMapping("/api/orders/guests/search")
    GuestOrderDetailResponse getGuestOrder(@RequestBody OrderGuestLoginRequest request);
}