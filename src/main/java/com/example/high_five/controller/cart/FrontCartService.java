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

    public CartListResponse getCartItems(String cookieHeader) {
        // null 대신 unpaged() 사용
        return cartService.getCartItems(cookieHeader, Pageable.unpaged()).getBody();
    }

    public void addToCart(String cookieHeader, CartAddRequest request, HttpServletResponse servletResponse) {
        ResponseEntity<CartAddResponse> response = cartService.addItemToCart(request, cookieHeader);
        syncCookie(response, servletResponse);
    }

    public void updateQuantity(String cookieHeader, CartItemUpdateRequest request) {
        cartService.updateQuantity(request, cookieHeader);
    }

    public void deleteItem(String cookieHeader, Long bookId) {
        cartService.deleteOneItem(bookId, cookieHeader);
    }

    public void clearCart(String cookieHeader) {
        cartService.deleteAllCartItem(cookieHeader);
    }

    public void mergeCart(String cookieHeader, HttpServletResponse servletResponse) {
        // 1. 백엔드 호출
        ResponseEntity<Void> response = cartService.mergeGuestCart(cookieHeader);
        // 2. 백엔드에서 "쿠키 지워라"라는 헤더가 오면 브라우저로 전달
        syncCookie(response, servletResponse);
    }

    public void deleteGuestCart(String cookieHeader, HttpServletResponse servletResponse) {
        // 1. 백엔드 호출
        ResponseEntity<Void> response = cartService.deleteGuestCartOnly(cookieHeader);
        // 2. 쿠키 동기화
        syncCookie(response, servletResponse);
    }
}