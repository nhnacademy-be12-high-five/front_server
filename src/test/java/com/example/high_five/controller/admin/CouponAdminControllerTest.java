package com.example.high_five.controller.admin;

import com.example.high_five.dto.book.BookPagedResponse;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.*;
import com.example.high_five.service.BookFeignClient;
import com.example.high_five.service.CategoryFeignClient;
import com.example.high_five.service.CouponService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CouponAdminControllerTest {

    @InjectMocks
    private CouponAdminController couponAdminController;

    @Mock
    private CouponService couponService;
    @Mock
    private BookFeignClient bookFeignClient;
    @Mock
    private CategoryFeignClient categoryFeignClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(couponAdminController).build();
    }

    @Test
    @DisplayName("쿠폰 페이지 조회 - 성공")
    void couponPage_Success() throws Exception {
        given(couponService.getAdminCoupons()).willReturn(Collections.emptyList());
        given(couponService.getAllPolicies()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/coupons/page"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/coupons"))
                .andExpect(model().attributeExists("coupons", "policies"));
    }

    @Test
    @DisplayName("쿠폰 페이지 조회 - 인증 실패(401) 시 로그인 리다이렉트")
    void couponPage_Unauthorized() throws Exception {
        Request request = Request.create(HttpMethod.GET, "url", Map.of(), null, null, null);
        given(couponService.getAdminCoupons()).willThrow(new FeignException.Unauthorized("401", request, null, null));

        mockMvc.perform(get("/admin/coupons/page"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login.html"));
    }

    @Test
    @DisplayName("쿠폰 정책 생성 - 성공")
    void createPolicy_Success() throws Exception {
        mockMvc.perform(post("/admin/coupons/policies/create")
                        .flashAttr("couponPolicyRequestDto", new CouponPolicyRequestDto()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("쿠폰 정책 생성 - Feign 에러 메시지 파싱 테스트")
    void createPolicy_FeignException() throws Exception {
        String errorBody = "{\"message\":\"Duplicate Policy\"}";
        Request request = Request.create(HttpMethod.POST, "url", Map.of(), errorBody.getBytes(StandardCharsets.UTF_8), null, null);
        FeignException exception = new FeignException.BadRequest("Bad Request", request, errorBody.getBytes(StandardCharsets.UTF_8), Map.of());

        doThrow(exception).when(couponService).createCouponPolicy(any());

        mockMvc.perform(post("/admin/coupons/policies/create")
                        .flashAttr("couponPolicyRequestDto", new CouponPolicyRequestDto()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Duplicate Policy"));
    }

    @Test
    @DisplayName("쿠폰 템플릿 생성 - 성공")
    void createCouponTemplate_Success() throws Exception {
        mockMvc.perform(post("/admin/coupons/create")
                        .flashAttr("couponCreateRequestDto", new CouponCreateRequestDto()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("쿠폰 상태 변경")
    void updateCouponStatus() throws Exception {
        mockMvc.perform(post("/admin/coupons/1/status")
                        .param("status", "ACTIVE"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("정책 비활성화 - 성공")
    void disablePolicy() throws Exception {
        mockMvc.perform(post("/admin/coupons/policies/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원 쿠폰 수동 지급 - 성공")
    void issueCouponManually_Success() throws Exception {
        mockMvc.perform(post("/admin/coupons/member-coupons/issue")
                        .param("userId", "1")
                        .param("couponId", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @DisplayName("회원 쿠폰 수동 지급 - 이미 보유(409)")
    void issueCouponManually_Conflict() throws Exception {
        Request request = Request.create(HttpMethod.POST, "url", Map.of(), null, null, null);
        FeignException exception = new FeignException.Conflict("Conflict", request, null, null);
        doThrow(exception).when(couponService).issueCouponByAdmin(any());

        mockMvc.perform(post("/admin/coupons/member-coupons/issue")
                        .param("userId", "1")
                        .param("couponId", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "이미 해당 쿠폰을 보유한 회원입니다."));
    }

    @Test
    @DisplayName("정책 상세 조회")
    void policyDetail() throws Exception {
        given(couponService.getCouponPolicy(1L)).willReturn(new CouponPolicyResponseDto());

        mockMvc.perform(get("/admin/coupons/policies/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/policy-detail"))
                .andExpect(model().attributeExists("policy"));
    }

    @Test
    @DisplayName("쿠폰용 도서 검색")
    void searchBooksForCoupon() throws Exception {
        // BookPagedResponse 객체 생성 및 Setter 사용
        BookPagedResponse<BookResponse> response = new BookPagedResponse<>();
        response.setContent(Collections.emptyList());

        given(bookFeignClient.searchBooks(anyString(), anyInt(), anyInt())).willReturn(response);

        mockMvc.perform(get("/admin/coupons/books/search").param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 도서 쿠폰 페이지")
    void specificBookCouponPage() throws Exception {
        given(couponService.getAllPolicies()).willReturn(Collections.emptyList());
        mockMvc.perform(get("/admin/coupons/specific-book-coupons"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/book-coupons"));
    }
}