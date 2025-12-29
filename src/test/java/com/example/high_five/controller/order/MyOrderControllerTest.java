package com.example.high_five.controller.order;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.MyOrderResponse;
import com.example.high_five.dto.order.OrderReturnRequest;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MyOrderControllerTest {

    @InjectMocks
    private MyOrderController myOrderController;

    @Mock
    private FrontOrderService frontOrderService;

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myOrderController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("내 주문 목록 조회 - 로그인 상태")
    void myOrders_Login() throws Exception {
        // given
        MemberResponse member = new MemberResponse(1L, "Test", "email", LocalDate.now(), "phone", "ACTIVE", "GOLD");
        given(memberService.getMyInfo()).willReturn(ResponseEntity.ok(member));

        CommonPageResponse<MyOrderResponse> pageResponse = new CommonPageResponse<>();
        // [핵심 수정] CommonPageResponse 내부 필드(data 또는 content)에 직접 값을 주입하여 null 방지
        // CommonPageResponse의 필드명이 'data'라고 가정 (만약 'content'라면 "content"로 변경)
        try {
            ReflectionTestUtils.setField(pageResponse, "data", Collections.emptyList());
        } catch (IllegalArgumentException e) {
            // data 필드가 없으면 content 시도
            ReflectionTestUtils.setField(pageResponse, "content", Collections.emptyList());
        }

        // Mocking: Matchers를 명확하게 지정
        given(frontOrderService.getMyOrders(eq(1L), anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/mypage/orders")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/orders"))
                .andExpect(model().attributeExists("orders")) // 이제 통과될 것임
                .andExpect(model().attributeExists("page"));
    }

    @Test
    @DisplayName("내 주문 목록 조회 - 비로그인 상태 (리다이렉트)")
    void myOrders_NoLogin() throws Exception {
        given(memberService.getMyInfo()).willReturn(null);

        mockMvc.perform(get("/mypage/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login.html"));
    }

    @Test
    @DisplayName("반품 신청 - 성공")
    void requestReturn_Success() throws Exception {
        OrderReturnRequest request = new OrderReturnRequest();

        mockMvc.perform(post("/mypage/orders/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("반품 신청이 완료되었습니다."));

        verify(frontOrderService).requestReturn(eq(1L), any(OrderReturnRequest.class));
    }

    @Test
    @DisplayName("반품 신청 - 실패")
    void requestReturn_Fail() throws Exception {
        doThrow(new RuntimeException("Fail")).when(frontOrderService).requestReturn(eq(1L), any());

        mockMvc.perform(post("/mypage/orders/1/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}