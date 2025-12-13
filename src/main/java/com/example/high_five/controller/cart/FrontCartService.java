package com.example.high_five.controller.cart;

import com.example.high_five.dto.cart.*;
import com.example.high_five.service.CartService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrontCartService {

    private final CartService cartService;

    // 백엔드 쿠키 동기화
    private void syncCookie(ResponseEntity<?> responseEntity, HttpServletResponse servletResponse) {
        if (responseEntity == null) return;
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            cookies.forEach(cookie -> servletResponse.addHeader("Set-Cookie", cookie));
        }
    }

    public CartListResponse getCartItems(Long memberId, String guestId) {
        // null 대신 unpaged() 사용
        return cartService.getCartItems(memberId, guestId, Pageable.unpaged()).getBody();
    }

    public void addToCart(Long memberId, String guestId, CartAddRequest request, HttpServletResponse servletResponse) {
        ResponseEntity<CartAddResponse> response = cartService.addItemToCart(request, memberId, guestId);
        syncCookie(response, servletResponse);
    }

    public void updateQuantity(Long memberId, String guestId, CartItemUpdateRequest request) {
        cartService.updateQuantity(request, memberId, guestId);
    }

    public void deleteItem(Long memberId, String guestId, Long bookId) {
        cartService.deleteOneItem(bookId, memberId, guestId);
    }

    public void clearCart(Long memberId, String guestId) {
        cartService.deleteAllCartItem(memberId, guestId);
    }

    public void mergeCart(Long memberId, String guestId, HttpServletResponse servletResponse) {
        // 1. 백엔드 호출
        ResponseEntity<Void> response = cartService.mergeGuestCart(memberId, guestId);
        // 2. 백엔드에서 "쿠키 지워라"라는 헤더가 오면 브라우저로 전달
        syncCookie(response, servletResponse);
    }

    public void deleteGuestCart(Long memberId, String guestId, HttpServletResponse servletResponse) {
        // 1. 백엔드 호출
        ResponseEntity<Void> response = cartService.deleteGuestCartOnly(memberId, guestId);
        // 2. 쿠키 동기화
        syncCookie(response, servletResponse);
    }
}