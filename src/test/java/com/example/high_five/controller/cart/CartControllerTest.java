package com.example.high_five.controller.cart;

import com.example.high_five.dto.cart.CartAddRequest;
import com.example.high_five.dto.cart.CartDetailResponse;
import com.example.high_five.dto.cart.CartItemUpdateRequest;
import com.example.high_five.dto.cart.CartListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private FrontCartService cartService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // [핵심 수정] StringHttpMessageConverter를 UTF-8로 강제 설정하여 등록
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8), // 문자열 반환 시 UTF-8 처리
                        new MappingJackson2HttpMessageConverter() // JSON 처리
                )
                .build();
    }

    @Test
    @DisplayName("장바구니 페이지 조회")
    void viewCartItems() throws Exception {
        Long memberId = 1L;
        // DTO 생성 (record 또는 class 구조에 맞게)
        CartListResponse mockResponse = new CartListResponse(Collections.emptyList(), 0, false);
        given(cartService.getCartItems(memberId)).willReturn(mockResponse);

        mockMvc.perform(get("/cart")
                        .header("X-USER-ID", memberId))
                .andExpect(status().isOk())
                .andExpect(view().name("order/cart"))
                .andExpect(model().attribute("cartList", mockResponse));
    }

    @Test
    @DisplayName("장바구니 담기")
    void addItem() throws Exception {
        CartAddRequest request = new CartAddRequest(1L, 2);

        mockMvc.perform(post("/cart/items")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("장바구니에 담겼습니다."));

        verify(cartService).addToCart(eq(1L), any(CartAddRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("장바구니 수량 변경")
    void updateQuantity() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(1L, 5);

        mockMvc.perform(put("/cart/items")
                        .header("X-USER-ID", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(cartService).updateQuantity(eq(1L), any(CartItemUpdateRequest.class));
    }

    @Test
    @DisplayName("장바구니 단건 삭제")
    void deleteItem() throws Exception {
        Long bookId = 100L;

        mockMvc.perform(delete("/cart/items/{bookId}", bookId)
                        .header("X-USER-ID", 1L))
                .andExpect(status().isNoContent());

        verify(cartService).deleteItem(1L, bookId);
    }

    @Test
    @DisplayName("장바구니 전체 삭제")
    void clearCart() throws Exception {
        mockMvc.perform(delete("/cart/items")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(1L);
    }

    @Test
    @DisplayName("장바구니 카운트 - 성공")
    void getCartCount_Success() throws Exception {
        // 아이템 2개가 있는 리스트 가정
        List<CartDetailResponse> items = List.of(new CartDetailResponse(1L, "title", 30000, 3, 90000, null), new CartDetailResponse(1L, "title", 30000, 3, 90000, null));
        CartListResponse response = new CartListResponse(items, 1000, false);

        given(cartService.getCartItems(1L)).willReturn(response);

        mockMvc.perform(get("/cart/count")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    @DisplayName("장바구니 카운트 - 예외 발생 시 0 반환")
    void getCartCount_Exception() throws Exception {
        given(cartService.getCartItems(any())).willThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/cart/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @DisplayName("장바구니 합치기 및 게스트 쿠키 만료")
    void mergeCart() throws Exception {
        mockMvc.perform(post("/cart/merge")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                // 쿠키 만료(Max-Age=0) 헤더 확인
                .andExpect(cookie().maxAge("guestCookie", 0));

        verify(cartService).mergeCart(eq(1L), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("게스트 장바구니 삭제 및 쿠키 만료")
    void deleteGuestCart() throws Exception {
        mockMvc.perform(delete("/cart/guest")
                        .header("X-USER-ID", 1L))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("guestCookie", 0));

        verify(cartService).deleteGuestCart(eq(1L), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("테스트 페이지 조회")
    void viewTestPage() throws Exception {
        mockMvc.perform(get("/bookTest"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/test-products"));
    }
}