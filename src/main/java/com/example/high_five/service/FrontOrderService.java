package com.example.high_five.service;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.DeliveryPolicyResponse;
import com.example.high_five.dto.order.MyOrderResponse;
import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrontOrderService {

    private final MemberService memberService;
    private final BookClient bookClient;
    private final OrderClient orderClient;
    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    /**
     * 주문서 작성 페이지 데이터 조회 (메인 메서드)
     */
    public OrderResponse createOrderSheet(Long userId, String token, List<Long> bookIds, List<Integer> quantities) {

        MemberInfoResult memberInfo = fetchMemberInfo(userId, token);

        OrderItemsResult itemsResult = createOrderItems(bookIds, quantities);

        List<OrderResponse.WrapperDto> wrappers = fetchWrappers();

        DeliveryPolicyResponse policy = fetchDeliveryPolicy();

        int deliveryFee = calculateDeliveryFee(itemsResult.totalProductPrice(), policy);

        return OrderResponse.builder()
                .name(memberInfo.member() != null ? memberInfo.member().getName() : "")
                .phoneNumber(memberInfo.member() != null ? memberInfo.member().getPhone() : "")
                .email(memberInfo.member() != null ? memberInfo.member().getEmail() : "")
                .myPoint(memberInfo.point())
                .coupons(memberInfo.coupons())
                .orderItems(itemsResult.items())
                .wrappers(wrappers)
                .totalProductPrice(itemsResult.totalProductPrice())
                .deliveryFee(deliveryFee)
                .deliveryPolicy(policy)
                .build();
    }

    /**
     * 주문 생성 요청
     */
    public OrderClient.OrderCreateResponse placeOrder(OrderCheckoutRequest request, Long userId, String guestId) {
        log.info("Order Request - UserID: {}, GuestID: {}, Items: {}", userId, guestId, request.getOrderItems().size());
        return orderClient.createOrder(userId, guestId, request);
    }

    private MemberInfoResult fetchMemberInfo(Long userId, String token) {
        if (userId == null) {
            return new MemberInfoResult(null, 0, Collections.emptyList());
        }

        MemberResponse member = null;
        Integer myPoint = 0;
        List<MemberCouponResponseDto> coupons = new ArrayList<>();

        try {
            // 기본 정보
            ResponseEntity<MemberResponse> memberResp = memberService.getMyInfo();
            if (memberResp != null && memberResp.getBody() != null) {
                member = memberResp.getBody();
            }

            if (token != null) {
                try {
                    ResponseEntity<PointBalanceResponse> pointResp = memberService.getMyBalance(token);
                    if (pointResp != null && pointResp.getBody() != null) {
                        Long currentPoint = pointResp.getBody().getCurrentPoint();
                        myPoint = currentPoint != null ? currentPoint.intValue() : 0;
                    }
                } catch (Exception e) {
                    log.warn("포인트 조회 실패 (userId={}): {}", userId, e.getMessage());
                }

                try {
                    Map<String, Object> couponPageMap = couponService.getMemberCoupons(token, 0, 100);
                    if (couponPageMap != null && couponPageMap.containsKey("content")) {
                        Object content = couponPageMap.get("content");
                        coupons = objectMapper.convertValue(content, new TypeReference<List<MemberCouponResponseDto>>() {});
                    }
                } catch (Exception e) {
                    log.warn("쿠폰 조회 실패: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("회원 정보 로드 실패", e);
        }

        return new MemberInfoResult(member, myPoint, coupons);
    }

    private OrderItemsResult createOrderItems(List<Long> bookIds, List<Integer> quantities) {
        List<OrderResponse.OrderItem> items = new ArrayList<>();
        int totalProductPrice = 0;

        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            Integer qty = quantities.get(i);

            try {
                BookResponse bookInfo = bookClient.getBookDetail(bookId);
                if (bookInfo != null) {
                    int price = bookInfo.getPrice();
                    int itemTotal = price * qty;

                    items.add(OrderResponse.OrderItem.builder()
                            .bookId(bookId)
                            .title(bookInfo.getTitle())
                            .imageUrl(bookInfo.getImage())
                            .price(price)
                            .quantity(qty)
                            .totalPrice(itemTotal)
                            .build());

                    totalProductPrice += itemTotal;
                }
            } catch (Exception e) {
                log.error("책 정보 조회 실패 (bookId={}): {}", bookId, e.getMessage());
            }
        }
        return new OrderItemsResult(items, totalProductPrice);
    }

    private List<OrderResponse.WrapperDto> fetchWrappers() {
        try {
            return orderClient.getWrappers();
        } catch (Exception e) {
            log.warn("포장지 목록 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private DeliveryPolicyResponse fetchDeliveryPolicy() {
        int defaultFee = 3000;
        int defaultThreshold = 30000;

        try {
            DeliveryPolicyResponse policy = orderClient.getDeliveryPolicy();
            if (policy != null) {
                if (policy.getStandardShippingFee() == null) {
                    policy = DeliveryPolicyResponse.builder()
                            .id(policy.getId())
                            .standardShippingFee(defaultFee)
                            .minOrderAmount(policy.getMinOrderAmount())
                            .build();
                }
                if (policy.getMinOrderAmount() == null) {
                    policy = DeliveryPolicyResponse.builder()
                            .id(policy.getId())
                            .standardShippingFee(policy.getStandardShippingFee())
                            .minOrderAmount(defaultThreshold)
                            .build();
                }
                return policy;
            }
        } catch (Exception e) {
            log.warn("배송 정책 조회 실패 (기본값 사용): {}", e.getMessage());
        }

        return DeliveryPolicyResponse.builder()
                .standardShippingFee(defaultFee)
                .minOrderAmount(defaultThreshold)
                .build();
    }

    private int calculateDeliveryFee(int totalProductPrice, DeliveryPolicyResponse policy) {
        int threshold = policy.getMinOrderAmount() != null ? policy.getMinOrderAmount() : 30000;
        int fee = policy.getStandardShippingFee() != null ? policy.getStandardShippingFee() : 3000;

        return (totalProductPrice >= threshold) ? 0 : fee;
    }

    public void cancelOrder(Long orderId) {

        try {
            orderClient.cancelOrder(orderId);
            log.info("Order Cancel Success: OrderID={}", orderId);
        } catch (Exception e) {
            log.error("Order Cancel Failed: OrderID={}", orderId, e);
            throw new RuntimeException("주문 취소 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 내 주문 내역 조회
    public CommonPageResponse<MyOrderResponse> getMyOrders(Long userId, int page, int size) {
        try {
            return orderClient.getMyOrders(userId, page, size);
        } catch (Exception e) {
            log.error("주문 내역 조회 실패 UserID={}: {}", userId, e.getMessage());
            return new CommonPageResponse<>();
        }
    }

    private record MemberInfoResult(MemberResponse member, Integer point, List<MemberCouponResponseDto> coupons) {}
    private record OrderItemsResult(List<OrderResponse.OrderItem> items, int totalProductPrice) {}
}