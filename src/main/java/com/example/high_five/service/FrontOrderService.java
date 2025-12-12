package com.example.high_five.service;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // [추가] Map 데이터를 DTO로 변환하기 위해 필요
    private final ObjectMapper objectMapper;

    /**
     * 주문서 작성 페이지에 필요한 모든 데이터를 조회하여 반환합니다.
     */
    public OrderResponse createOrderSheet(Long userId, String token, List<Long> bookIds, List<Integer> quantities) {

        // 1. 초기값 설정
        MemberResponse member = null;
        Integer myPoint = 0;
        List<MemberCouponResponseDto> coupons = new ArrayList<>();

        // 2. 회원 정보 조회 (로그인 시에만 실행)
        if (userId != null) {
            try {
                // 2-1. 회원 기본 정보 조회
                ResponseEntity<MemberResponse> memberResp = memberService.getMyInfo();
                if (memberResp != null && memberResp.getBody() != null) {
                    member = memberResp.getBody();
                }

                // 2-2. 포인트 잔액 조회 (Authorization 헤더 필요)
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

                // 2-3. 보유 쿠폰 조회 (토큰 필요)
                if (token != null) {
                    try {
                        // [수정] CouponService 파라미터에 맞게 호출 (AccessToken, Page, Size)
                        // size를 넉넉하게 잡아(예: 100) 사용 가능한 쿠폰을 한 번에 가져오도록 처리
                        Map<String, Object> couponPageMap = couponService.getMemberCoupons(token, 0, 100);

                        // [수정] Map<String, Object> 응답에서 "content" 키를 꺼내 리스트로 변환
                        if (couponPageMap != null && couponPageMap.containsKey("content")) {
                            Object content = couponPageMap.get("content");
                            coupons = objectMapper.convertValue(content, new TypeReference<List<MemberCouponResponseDto>>() {});
                        }
                    } catch (Exception e) {
                        log.warn("쿠폰 조회 실패: {}", e.getMessage());
                        // 실패 시 빈 리스트로 처리하여 주문서 생성은 계속 진행되도록 함
                        coupons = Collections.emptyList();
                    }
                }

            } catch (Exception e) {
                log.error("회원 정보 로드 실패", e);
            }
        }

        // 3. 도서 정보 조회 (Book Server)
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

        // 4. 포장지 목록 조회 (Order Server)
        List<OrderResponse.WrapperDto> wrappers = new ArrayList<>();
        try {
            wrappers = orderClient.getWrappers();
        } catch (Exception e) {
            log.warn("포장지 목록 조회 실패: {}", e.getMessage());
        }

        // 5. 배송비 계산 (단순 로직: 3만원 이상 무료)
        int deliveryFee = (totalProductPrice >= 30000) ? 0 : 3000;

        // 6. 최종 응답 객체 생성
        return OrderResponse.builder()
                .name(member != null ? member.getName() : "")
                .phoneNumber(member != null ? member.getPhone() : "")
                .email(member != null ? member.getEmail() : "")
                .myPoint(myPoint)
                .coupons(coupons)
                .orderItems(items)
                .wrappers(wrappers)
                .totalProductPrice(totalProductPrice)
                .deliveryFee(deliveryFee)
                .build();
    }

    /**
     * 주문 생성 요청을 Order Server로 전송합니다.
     */
    public OrderClient.OrderCreateResponse placeOrder(OrderCheckoutRequest request, Long userId) {

        log.info("Order Request - UserID: {}, Items: {}", userId, request.getOrderItems().size());

        return orderClient.createOrder(userId, request);
    }
}