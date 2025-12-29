package com.example.high_five.controller.cart;

import com.example.high_five.dto.cart.CartAddRequest;
import com.example.high_five.dto.cart.CartAddResponse;
import com.example.high_five.dto.cart.CartItemUpdateRequest;
import com.example.high_five.dto.cart.CartListResponse;
import com.example.high_five.service.CartService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FrontCartServiceTest {

    @InjectMocks
    private FrontCartService frontCartService;

    @Mock
    private CartService cartService; // Feign Client

    @Test
    @DisplayName("장바구니 조회 - unpaged() 사용 확인")
    void getCartItems() {
        // given
        Long memberId = 1L;
        CartListResponse mockResponse = new CartListResponse(Collections.emptyList(), 0, false);
        given(cartService.getCartItems(eq(memberId), any(Pageable.class)))
                .willReturn(ResponseEntity.ok(mockResponse));

        // when
        CartListResponse result = frontCartService.getCartItems(memberId);

        // then
        assertThat(result).isEqualTo(mockResponse);
        // Pageable.unpaged()가 호출되었는지 간접 검증 (ArgumentCaptor 등을 쓸 수도 있으나 여기선 호출 여부만)
        verify(cartService).getCartItems(eq(memberId), any(Pageable.class));
    }

    @Test
    @DisplayName("장바구니 담기 - 쿠키 동기화 확인")
    void addToCart() {
        // given
        CartAddRequest request = new CartAddRequest(1L, 1);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // 백엔드에서 Set-Cookie 헤더가 담긴 응답이 왔다고 가정
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "guest=abc; Path=/");
        ResponseEntity<CartAddResponse> backendResponse = ResponseEntity.ok().headers(headers).build();

        given(cartService.addItemToCart(request, 1L)).willReturn(backendResponse);

        // when
        frontCartService.addToCart(1L, request, servletResponse);

        // then
        // syncCookie 로직에 의해 서블릿 응답에 헤더가 추가되어야 함
        verify(servletResponse).addHeader("Set-Cookie", "guest=abc; Path=/");
    }

    @Test
    @DisplayName("수량 변경")
    void updateQuantity() {
        CartItemUpdateRequest request = new CartItemUpdateRequest(1L, 5);
        frontCartService.updateQuantity(1L, request);
        verify(cartService).updateQuantity(request, 1L);
    }

    @Test
    @DisplayName("단건 삭제")
    void deleteItem() {
        frontCartService.deleteItem(1L, 100L);
        verify(cartService).deleteOneItem(100L, 1L);
    }

    @Test
    @DisplayName("전체 삭제")
    void clearCart() {
        frontCartService.clearCart(1L);
        verify(cartService).deleteAllCartItem(1L);
    }

    @Test
    @DisplayName("장바구니 합치기 - 쿠키 동기화 확인")
    void mergeCart() {
        // given
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, "merged=true");
        ResponseEntity<Void> backendResponse = ResponseEntity.ok().headers(headers).build();

        given(cartService.mergeGuestCart(1L)).willReturn(backendResponse);

        // when
        frontCartService.mergeCart(1L, servletResponse);

        // then
        verify(servletResponse).addHeader("Set-Cookie", "merged=true");
    }

    @Test
    @DisplayName("게스트 장바구니 삭제 - 쿠키 동기화 확인")
    void deleteGuestCart() {
        // given
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        given(cartService.deleteGuestCartOnly(1L)).willReturn(ResponseEntity.ok().build());

        // when
        frontCartService.deleteGuestCart(1L, servletResponse);

        // then
        verify(cartService).deleteGuestCartOnly(1L);
        // 헤더가 없으면 addHeader 호출 안 함
    }
}