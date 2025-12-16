package com.example.high_five.service;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.DeliveryPolicyResponse;
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

    public OrderResponse createOrderSheet(Long userId, String token, List<Long> bookIds, List<Integer> quantities) {

        MemberDiscountInfo memberInfo = fetchMemberDiscountInfo(userId, token);

        ItemsCalculationResult itemsResult = calculateProductPriceAndItems(bookIds, quantities);

        List<OrderResponse.WrapperDto> wrappers = fetchWrappers();

        DeliveryPolicyResult deliveryResult = fetchDeliveryPolicyAndCalculateFee(itemsResult.totalProductPrice());

        return OrderResponse.builder()
                .name(memberInfo.member() != null ? memberInfo.member().getName() : "")
                .phoneNumber(memberInfo.member() != null ? memberInfo.member().getPhone() : "")
                .email(memberInfo.member() != null ? memberInfo.member().getEmail() : "")
                .myPoint(memberInfo.myPoint())
                .coupons(memberInfo.coupons())
                .orderItems(itemsResult.items())
                .wrappers(wrappers)
                .totalProductPrice(itemsResult.totalProductPrice())
                .deliveryFee(deliveryResult.deliveryFee())
                .deliveryPolicy(deliveryResult.policy())
                .build();
    }

    public OrderClient.OrderCreateResponse placeOrder(OrderCheckoutRequest request, Long userId, String guestId) {
        log.info("Order Request - UserID: {}, GuestID: {}, Items: {}", userId, guestId, request.getOrderItems().size());
        return orderClient.createOrder(userId, guestId, request);
    }

    public void cancelOrder(Long orderId) {
        try {
            orderClient.cancelOrder(orderId);
        } catch (Exception e) {
            log.error("주문 취소 요청 실패 (orderId={})", orderId, e);
        }
    }

    private MemberDiscountInfo fetchMemberDiscountInfo(Long userId, String token) {
        MemberResponse member = null;
        Integer myPoint = 0;
        List<MemberCouponResponseDto> coupons = new ArrayList<>();

        if (userId != null) {
            try {
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
                }

                if (token != null) {
                    try {
                        Map<String, Object> couponPageMap = couponService.getMemberCoupons(token, 0, 100);
                        if (couponPageMap != null && couponPageMap.containsKey("content")) {
                            Object content = couponPageMap.get("content");
                            coupons = objectMapper.convertValue(content, new TypeReference<List<MemberCouponResponseDto>>() {});
                        }
                    } catch (Exception e) {
                        log.warn("쿠폰 조회 실패: {}", e.getMessage());
                        coupons = Collections.emptyList();
                    }
                }
            } catch (Exception e) {
                log.error("회원 정보 로드 실패", e);
            }
        }
        return new MemberDiscountInfo(member, myPoint, coupons);
    }

    private ItemsCalculationResult calculateProductPriceAndItems(List<Long> bookIds, List<Integer> quantities) {
        List<OrderResponse.OrderItem> items = new ArrayList<>();
        int totalProductPrice = 0;

        for (int i = 0; i < bookIds.size(); i++) {
            Long bookId = bookIds.get(i);
            Integer qty = quantities.get(i);

            BookResponse bookInfo = null;
            try {
                bookInfo = bookClient.getBookDetail(bookId);
            } catch (Exception e) {
                log.error("책 정보 조회 실패 (bookId={}): {}", bookId, e.getMessage());
                continue;
            }

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
        }
        return new ItemsCalculationResult(items, totalProductPrice);
    }

    private List<OrderResponse.WrapperDto> fetchWrappers() {
        try {
            return orderClient.getWrappers();
        } catch (Exception e) {
            log.warn("포장지 목록 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private DeliveryPolicyResult fetchDeliveryPolicyAndCalculateFee(int totalProductPrice) {
        int deliveryFee = 0;
        DeliveryPolicyResponse policy;

        try {
            policy = orderClient.getCurrentDeliveryPolicy();

            long minOrderAmount = (policy.getMinOrderAmount() != null) ? policy.getMinOrderAmount() : 30000L;
            int standardFee = (policy.getStandardShippingFee() != null) ? policy.getStandardShippingFee() : 3000;

            if (totalProductPrice < minOrderAmount) {
                deliveryFee = standardFee;
            }

        } catch (Exception e) {
            log.warn("배송 정책 조회 실패, 기본값 적용: {}", e.getMessage());

            policy = DeliveryPolicyResponse.builder()
                    .minOrderAmount(30000)
                    .standardShippingFee(3000)
                    .remoteAreaSurcharge(5000)
                    .build();

            deliveryFee = (totalProductPrice >= 30000) ? 0 : 3000;
        }
        return new DeliveryPolicyResult(deliveryFee, policy);
    }


    @Builder
    private record MemberDiscountInfo(
            MemberResponse member,
            Integer myPoint,
            List<MemberCouponResponseDto> coupons
    ) {}

    private record ItemsCalculationResult(
            List<OrderResponse.OrderItem> items,
            int totalProductPrice
    ) {}

    private record DeliveryPolicyResult(
            int deliveryFee,
            DeliveryPolicyResponse policy
    ) {}
}