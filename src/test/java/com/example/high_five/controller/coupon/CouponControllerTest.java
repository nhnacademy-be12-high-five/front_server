package com.example.high_five.controller.coupon;

import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.coupon.UserCouponIssueRequestDto;
import com.example.high_five.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CouponControllerTest {

    @InjectMocks
    private CouponController couponController;

    @Mock
    private CouponService couponService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper(); // 실제 변환 로직 테스트를 위해 Spy 사용

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponController).build();
    }

    @Test
    @DisplayName("쿠폰 발급 페이지 조회 - 정상 데이터 파싱")
    void couponRegisterPage_Success() throws Exception {
        // given
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> couponItem = new HashMap<>();
        couponItem.put("templateId", 1L);
        couponItem.put("name", "Test Coupon"); // CouponTemplateDto 필드명에 맞춤 (만약 다르다면 수정 필요)

        mockResponse.put("content", List.of(couponItem));

        given(couponService.getIssuableCoupons(anyInt(), anyInt())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/coupon"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/coupon-register"))
                .andExpect(model().attributeExists("templates"));
    }

    @Test
    @DisplayName("쿠폰 발급 페이지 조회 - 예외 발생 시 빈 리스트 반환")
    void couponRegisterPage_Exception() throws Exception {
        // given
        given(couponService.getIssuableCoupons(anyInt(), anyInt())).willThrow(new RuntimeException("Service Down"));

        // when & then
        mockMvc.perform(get("/coupon"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/coupon-register"))
                .andExpect(model().attribute("templates", Collections.emptyList()));
    }

    @Test
    @DisplayName("쿠폰 발급 - 로그인 안됨 (쿠키 없음)")
    void issueCoupon_NoLogin() throws Exception {
        mockMvc.perform(post("/coupon/issue")
                        .param("couponId", "1")
                        .header("Referer", "/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"))
                .andExpect(flash().attribute("errorMessage", "로그인이 필요한 서비스입니다."));
    }

    @Test
    @DisplayName("쿠폰 발급 - 성공")
    void issueCoupon_Success() throws Exception {
        // given
        String token = "valid_token";
        String referer = "/books/1";

        // when
        mockMvc.perform(post("/coupon/issue")
                        .param("couponId", "10")
                        .cookie(new Cookie("access-token", token))
                        .header("Referer", referer))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(referer))
                .andExpect(flash().attribute("message", "쿠폰이 성공적으로 발급되었습니다."));

        // then
        verify(couponService).issueCoupon(eq("Bearer " + token), any(UserCouponIssueRequestDto.class));
    }

    @Test
    @DisplayName("쿠폰 발급 - 이미 발급된 쿠폰 (409)")
    void issueCoupon_Conflict() throws Exception {
        // given
        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(409);

        doThrow(exception).when(couponService).issueCoupon(anyString(), any());

        // when & then
        mockMvc.perform(post("/coupon/issue")
                        .param("couponId", "10")
                        .cookie(new Cookie("access-token", "token"))
                        .header("Referer", "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "이미 발급받은 쿠폰입니다."));
    }

    @Test
    @DisplayName("쿠폰 발급 - 잘못된 요청 (400)")
    void issueCoupon_BadRequest() throws Exception {
        // given
        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(400);

        doThrow(exception).when(couponService).issueCoupon(anyString(), any());

        // when & then
        mockMvc.perform(post("/coupon/issue")
                        .param("couponId", "10")
                        .cookie(new Cookie("access-token", "token")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "잘못된 요청입니다."));
    }

    @Test
    @DisplayName("쿠폰 발급 - 기타 오류 및 Referer 없을 때")
    void issueCoupon_GeneralError_NoReferer() throws Exception {
        // given
        doThrow(new RuntimeException("System Error")).when(couponService).issueCoupon(anyString(), any());

        // when & then
        mockMvc.perform(post("/coupon/issue")
                        .param("couponId", "10")
                        .cookie(new Cookie("access-token", "token"))) // Referer 헤더 생략 -> Default "/"
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "시스템 오류가 발생했습니다."));
    }

    @Test
    @DisplayName("마이 쿠폰 페이지 - 로그인 안됨 (리다이렉트)")
    void myCouponPage_NoLogin() throws Exception {
        mockMvc.perform(get("/mypage/coupons"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    @DisplayName("마이 쿠폰 페이지 - 정상 조회 및 페이징")
    void myCouponPage_Success() throws Exception {
        // given
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> couponMap = new HashMap<>();

        // [수정] DTO 필드명과 일치하도록 키 값 수정
        couponMap.put("id", 100L);           // couponId -> id
        couponMap.put("couponName", "My Coupon"); // name -> couponName
        couponMap.put("status", "UNUSED"); // 추가적으로 필요한 필드들 (에러 방지용)
        couponMap.put("discountType", "AMOUNT");

        mockResponse.put("content", List.of(couponMap));
        mockResponse.put("totalPages", 5);
        mockResponse.put("totalElements", 50);

        given(couponService.getMemberCoupons(anyString(), anyInt(), anyInt())).willReturn(mockResponse);

        // when
        mockMvc.perform(get("/mypage/coupons")
                        .cookie(new Cookie("access-token", "token"))
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/coupons"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 5))
                .andExpect(model().attribute("couponCount", 50L))
                .andExpect(model().attributeExists("myCoupons"));
    }

    @Test
    @DisplayName("마이 쿠폰 페이지 - 인증 실패 (401)")
    void myCouponPage_Unauthorized() throws Exception {
        // given
        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(401);

        given(couponService.getMemberCoupons(anyString(), anyInt(), anyInt())).willThrow(exception);

        // when & then
        mockMvc.perform(get("/mypage/coupons")
                        .cookie(new Cookie("access-token", "invalid")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    @DisplayName("마이 쿠폰 페이지 - 일반 시스템 오류")
    void myCouponPage_SystemError() throws Exception {
        // given
        given(couponService.getMemberCoupons(anyString(), anyInt(), anyInt())).willThrow(new RuntimeException("Error"));

        // when & then
        mockMvc.perform(get("/mypage/coupons")
                        .cookie(new Cookie("access-token", "token")))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/coupons"))
                .andExpect(model().attribute("myCoupons", Collections.emptyList())); // 빈 리스트 확인
    }
}