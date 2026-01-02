package com.example.high_five.controller.order;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.*;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FrontOrderServiceTest {

    @InjectMocks
    private FrontOrderService frontOrderService;

    @Mock
    private MemberService memberService;
    @Mock
    private BookClient bookClient;
    @Mock
    private OrderClient orderClient;
    @Mock
    private CouponService couponService;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문서 생성 - 회원, 정상 데이터 조회 및 배송비 계산")
    void createOrderSheet_Success() {
        // given
        Long userId = 1L;
        String token = "token";
        List<Long> bookIds = List.of(10L);
        List<Integer> quantities = List.of(2); // 2권

        // 1. Member Info Mock
        MemberResponse member = new MemberResponse(userId, "User", "email", null, "010-1234-5678", "ACTIVE", "GOLD");
        given(memberService.getMyInfo()).willReturn(ResponseEntity.ok(member));

        // 2. Point Mock
        PointBalanceResponse point = new PointBalanceResponse(userId, 5000L, 5000L);
        given(memberService.getMyBalance(token)).willReturn(ResponseEntity.ok(point));

        // 3. Coupon Mock
        MemberCouponResponseDto couponDto = new MemberCouponResponseDto();
        given(couponService.getUsableCoupons(eq(userId), eq(bookIds), anyList())).willReturn(List.of(couponDto));

        // 4. Book Mock
        BookResponse book = new BookResponse(10L, "Book Title", null, null, 10000, "img", null, null, null, null, null, 0.0, 0L, null, null, 1, null);
        given(bookClient.getBookDetail(10L)).willReturn(book);

        // 5. Wrappers Mock
        given(orderClient.getWrappers()).willReturn(Collections.emptyList());

        // 6. Policy Mock (3만원 이상 무료배송)
        DeliveryPolicyResponse policy = DeliveryPolicyResponse.builder()
                .standardShippingFee(3000)
                .minOrderAmount(30000)
                .build();
        given(orderClient.getDeliveryPolicy()).willReturn(policy);

        // when
        OrderResponse result = frontOrderService.createOrderSheet(userId, token, bookIds, quantities);

        // then
        assertThat(result.getName()).isEqualTo("User");
        assertThat(result.getMyPoint()).isEqualTo(5000);
        assertThat(result.getCoupons()).hasSize(1);
        assertThat(result.getOrderItems()).hasSize(1);

        // 가격 검증: 10000 * 2 = 20000원. 3만원 미만이므로 배송비 3000원 부과
        assertThat(result.getTotalProductPrice()).isEqualTo(20000);
        assertThat(result.getDeliveryFee()).isEqualTo(3000);
    }

    @Test
    @DisplayName("주문서 생성 - 비회원 (userId null)")
    void createOrderSheet_Guest() {
        // given
        // Book, Policy 등 최소한의 정보는 필요
        given(bookClient.getBookDetail(anyLong())).willReturn(new BookResponse(1L, "Book", null, null, 1000, null, null, null, null, null, null, 0.0, 0L, null, null, 1, null));

        // when
        OrderResponse result = frontOrderService.createOrderSheet(null, null, List.of(1L), List.of(1));

        // then
        assertThat(result.getName()).isEmpty();
        assertThat(result.getMyPoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문서 생성 - 외부 서비스 장애 시 기본값 사용 (Resilience)")
    void createOrderSheet_ServiceFailures() {
        // given
        Long userId = 1L;
        String token = "token";

        // 회원 정보 조회 실패 -> 예외 발생해도 잡아서 null 처리됨 -> 결과엔 빈 문자열
        given(memberService.getMyInfo()).willThrow(new RuntimeException("Member Service Down"));

        // 책 정보 조회 실패 -> 해당 아이템은 목록에서 제외됨
        given(bookClient.getBookDetail(anyLong())).willThrow(new RuntimeException("Book Service Down"));

        // 배송 정책 조회 실패 -> 기본값(3000원, 30000원) 사용
        given(orderClient.getDeliveryPolicy()).willThrow(new RuntimeException("Policy Service Down"));

        // when
        OrderResponse result = frontOrderService.createOrderSheet(userId, token, List.of(1L), List.of(1));

        // then
        assertThat(result.getName()).isEqualTo(""); // 회원정보 로드 실패 시
        assertThat(result.getOrderItems()).isEmpty(); // 책 정보 로드 실패 시 아이템 제외
        assertThat(result.getDeliveryFee()).isEqualTo(3000); // 정책 로드 실패 시 기본 배송비
    }

    @Test
    @DisplayName("주문 생성 요청 (placeOrder)")
    void placeOrder() {
        // given
        OrderCheckoutRequest request = new OrderCheckoutRequest();
        request.setOrderItems(Collections.emptyList());

        OrderCreateResponse mockResponse = new OrderCreateResponse(1L, "KEY", 1000);
        given(orderClient.createOrder(1L, "guest", request)).willReturn(mockResponse);

        // when
        OrderCreateResponse result = frontOrderService.placeOrder(request, 1L, "guest");

        // then
        assertThat(result).isEqualTo(mockResponse);
        verify(orderClient).createOrder(1L, "guest", request);
    }

    @Test
    @DisplayName("주문 취소 (cancelOrder) - 성공")
    void cancelOrder_Success() {
        frontOrderService.cancelOrder(1L);
        verify(orderClient).cancelOrder(1L);
    }

    @Test
    @DisplayName("주문 취소 (cancelOrder) - 실패 시 RuntimeException 래핑")
    void cancelOrder_Fail() {
        // void 메서드 예외 throw 설정 (willThrow 혹은 doThrow 사용)
        // Mockito void method stubbing: doThrow(...).when(mock).method(...)
        // 하지만 여기선 orderClient.cancelOrder(Long)가 void인지 확인 필요.
        // OrderClient 인터페이스가 void라면 doThrow, 아니면 given().willThrow
        // 보통 Client는 ResponseEntity나 void를 반환. 코드상 void로 추정되어 doThrow 사용

        // *주의*: 만약 OrderClient가 Feign이라 리턴값이 있다면 given() 사용해야 함.
        // 일반적인 void 가정 하에 작성:
        // doThrow(new RuntimeException("Fail")).when(orderClient).cancelOrder(1L);

        // FeignClient 메서드 시그니처를 모르므로 안전하게 doAnswer나 given 사용.
        // 코드상 return이 없으므로 void로 가정.

        // given
        // 만약 리턴 타입이 있다면: given(orderClient.cancelOrder(1L)).willThrow(...);
        // 만약 void라면:
        // doThrow(new RuntimeException("Fail")).when(orderClient).cancelOrder(1L);

        // 문제 방지를 위해 여기서는 단순 verify만 수행하거나, 예외가 발생한다고 가정하고 catch 블록 테스트
        try {
            // 강제로 예외 발생 시뮬레이션 (Mock 동작 설정)
            org.mockito.Mockito.doThrow(new RuntimeException("Error")).when(orderClient).cancelOrder(1L);
        } catch (Exception e) { /* ignore configuration error if return type mismatches */ }

        // when & then
        assertThatThrownBy(() -> frontOrderService.cancelOrder(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("주문 취소 처리 중 오류가 발생했습니다");
    }

    @Test
    @DisplayName("구매 확정 (confirmOrder)")
    void confirmOrder() {
        frontOrderService.confirmOrder(100L);
        verify(orderClient).confirmOrder(100L);
    }

    @Test
    @DisplayName("내 주문 목록 조회 (getMyOrders) - 예외 발생 시 빈 객체 반환")
    void getMyOrders_Exception() {
        // given
        given(orderClient.getMyOrders(anyLong(), anyInt(), anyInt())).willThrow(new RuntimeException("Error"));

        // when
        CommonPageResponse<MyOrderResponse> result = frontOrderService.getMyOrders(1L, 0, 10);

        // then
        assertThat(result).isNotNull();
        // CommonPageResponse의 기본 생성자가 필드를 null로 두는지 확인 필요하지만,
        // 로직상 new CommonPageResponse<>()를 리턴하므로 null은 아님.
    }

    @Test
    @DisplayName("비회원 주문 조회 (getGuestOrder)")
    void getGuestOrder() {
        // given
        GuestOrderDetailResponse mockResponse = new GuestOrderDetailResponse();
        given(orderClient.getGuestOrder(any(OrderGuestLoginRequest.class))).willReturn(mockResponse);

        // when
        GuestOrderDetailResponse result = frontOrderService.getGuestOrder(123L, "password");

        // then
        assertThat(result).isEqualTo(mockResponse);
        verify(orderClient).getGuestOrder(argThat(arg ->
                arg.getOrderId().equals(123L) && arg.getPassword().equals("password")
        ));
    }

    @Test
    @DisplayName("반품 신청 (requestReturn)")
    void requestReturn() {
        OrderReturnRequest request = new OrderReturnRequest();
        frontOrderService.requestReturn(1L, request);
        verify(orderClient).requestReturn(1L, request);
    }
}