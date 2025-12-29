package com.example.high_five.controller.admin;

import com.example.high_five.dto.payment.MethodStatusRequest;
import com.example.high_five.dto.payment.PaymentStatsResponse;
import com.example.high_five.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentAdminControllerTest {

    @InjectMocks
    private PaymentAdminController paymentAdminController;

    @Mock
    private PaymentService paymentClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentAdminController).build();
    }

    @Test
    @DisplayName("결제 수단 목록 조회 - 실패 시 에러 메시지")
    void getPaymentMethods_Fail() throws Exception {
        given(paymentClient.getAllMethods()).willThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("결제 수단 상태 변경")
    void updateStatus() throws Exception {
        mockMvc.perform(post("/admin/payments/1/status")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection());

        verify(paymentClient).updateStatus(eq(1L), any(MethodStatusRequest.class));
    }

    @Test
    @DisplayName("결제 통계 페이지 조회 - 날짜 미입력 시 기본값")
    void paymentStatsPage_DefaultDates() throws Exception {
        given(paymentClient.getTotalStats()).willReturn(new PaymentStatsResponse(0L, 0L, 0L, 0L, 0L, 0L));
        // 날짜가 null이면 오늘 날짜 등이 들어가므로 any()로 매칭
        given(paymentClient.getDailyStats(any(LocalDate.class), any(LocalDate.class))).willReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/payments/stats"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("summary", "dailyStats", "startDate", "endDate"));
    }
}