package com.example.high_five.controller.order;

import com.example.high_five.dto.order.*;
import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import com.example.high_five.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private FrontOrderService frontOrderService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private MemberService memberService;
    @Mock
    private CouponService couponService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
        ReflectionTestUtils.setField(orderController, "tossClientKey", "test_key");
    }

    @Test
    @DisplayName("주문서 작성 페이지 조회")
    void orderSheet() throws Exception {
        OrderResponse mockOrderSheet = new OrderResponse();
        mockOrderSheet.setName("Test User");
        given(frontOrderService.createOrderSheet(any(), any(), any(), any())).willReturn(mockOrderSheet);

        PointBalanceResponse point = new PointBalanceResponse(1L, 1000L, 5000L);
        given(memberService.getMyBalance(any())).willReturn(ResponseEntity.ok(point));

        given(couponService.getUsableCoupons(any(), any())).willReturn(Collections.emptyList());
        given(paymentService.getAllMethods()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/orders/sheet")
                        .param("bookIds", "1,2")
                        .param("quantities", "1,1")
                        .header("X-USER-ID", 1L)
                        .header("Authorization", "token"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/order"))
                .andExpect(model().attributeExists("orderSheet", "point", "coupons", "paymentMethods"));
    }

    @Test
    @DisplayName("주문 생성 API - 성공")
    void createOrderApi_Success() throws Exception {
        OrderCreateResponse response = new OrderCreateResponse(100L, "ORDER-KEY-123", 15000);
        given(frontOrderService.placeOrder(any(), any(), any())).willReturn(response);

        mockMvc.perform(post("/orders/api/create")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("receiverName", "Test")
                        .param("receiverPhone", "010-1234-5678")
                        .param("zipCode", "12345")
                        .param("address", "Addr")
                        .param("detailAddress", "Detail"))
                .andExpect(status().isOk())
                // [수정] DTO 필드명 orderKey로 검증
                .andExpect(jsonPath("$.orderKey").value("ORDER-KEY-123"))
                .andExpect(jsonPath("$.totalAmount").value(15000));
    }

    @Test
    @DisplayName("주문 생성 API - 실패 (서비스 예외 발생)")
    void createOrderApi_Fail() throws Exception {
        // [수정] Validation 오류 대신 서비스 예외를 발생시켜 400 Bad Request 검증
        given(frontOrderService.placeOrder(any(), any(), any())).willThrow(new RuntimeException("주문 실패"));

        mockMvc.perform(post("/orders/api/create")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("receiverName", "Test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("주문 실패")));
    }

    @Test
    @DisplayName("결제 승인 - 성공")
    void paymentSuccess() throws Exception {
        PaymentConfirmResponse response = PaymentConfirmResponse.builder()
                .paymentId(1L)
                .orderId(123L)
                .amount(10000L)
                .status(null)
                .build();

        given(paymentService.confirmPayment(any(PaymentConfirmRequest.class))).willReturn(response);

        mockMvc.perform(get("/orders/success")
                        .param("paymentKey", "pay_key")
                        .param("orderId", "ORDER-123")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/payment-success"))
                .andExpect(model().attribute("totalPrice", 10000L));
    }

    @Test
    @DisplayName("결제 승인 - 실패 (FeignException)")
    void paymentSuccess_FeignException() throws Exception {
        // [수정] NPE 방지를 위해 Request 객체 생성
        Request request = Request.create(HttpMethod.POST, "/confirm", Map.of(), null, null, null);
        FeignException exception = new FeignException.BadRequest("Bad Request", request, null, null);

        given(paymentService.confirmPayment(any())).willThrow(exception);

        mockMvc.perform(get("/orders/success")
                        .param("paymentKey", "pay_key")
                        .param("orderId", "ORDER-123")
                        .param("amount", "10000"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/payment-fail"))
                .andExpect(model().attribute("code", "PAYMENT_CONFIRM_ERROR"));
    }

    @Test
    @DisplayName("주문 취소 API")
    void cancelOrder() throws Exception {
        mockMvc.perform(post("/orders/1/cancel"))
                .andExpect(status().isOk());

        verify(frontOrderService).cancelOrder(1L);
    }

    @Test
    @DisplayName("구매 확정 API")
    void confirmOrder() throws Exception {
        mockMvc.perform(post("/orders/1/confirm"))
                .andExpect(status().isOk());

        verify(frontOrderService).confirmOrder(1L);
    }

    @Test
    @DisplayName("비회원 주문 검증 API - 성공")
    void validateGuestOrder_Success() throws Exception {
        Map<String, Object> request = Map.of("orderId", 123, "password", "pass");

        mockMvc.perform(post("/orders/guest/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(frontOrderService).getGuestOrder(123L, "pass");
    }

    @Test
    @DisplayName("비회원 주문 조회 페이지 - 성공")
    void getGuestOrder_Success() throws Exception {
        GuestOrderDetailResponse response = new GuestOrderDetailResponse();
        given(frontOrderService.getGuestOrder(123L, "pass")).willReturn(response);

        mockMvc.perform(post("/orders/guest")
                        .param("orderId", "123")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/guest-order-detail"));
    }
}