package com.example.high_five.controller.admin;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.order.OrderStatusUpdateRequest;
import com.example.high_five.service.OrderClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderAdminControllerTest {

    @InjectMocks
    private OrderAdminController orderAdminController;

    @Mock
    private OrderClient orderClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderAdminController).build();
    }

    @Test
    @DisplayName("주문 관리 페이지 조회 - 데이터 있음")
    void getOrderPage_WithData() throws Exception {
        CommonPageResponse<OrderResponse> pageResponse = new CommonPageResponse<>();
        pageResponse.setData(Collections.emptyList());

        given(orderClient.getAdminOrders(anyInt(), anyInt(), any())).willReturn(pageResponse);

        mockMvc.perform(get("/admin/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"))
                .andExpect(model().attributeExists("orders", "pageInfo", "selectedStatus"));
    }

    @Test
    @DisplayName("주문 상태 변경 API")
    void updateStatusApi() throws Exception {
        // 생성자 인자 2개 (status, trackingNumber)
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("SHIPPING", "TRACK-12345");

        mockMvc.perform(put("/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPING\", \"trackingNumber\":\"TRACK-12345\"}"))
                .andExpect(status().isOk());

        verify(orderClient).updateOrderStatus(eq(1L), any(OrderStatusUpdateRequest.class));
    }
}