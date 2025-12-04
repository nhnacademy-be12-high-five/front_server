package com.example.high_five.controller.cart;
import com.example.high_five.dto.cart.*;
import com.example.high_five.service.CartFeignClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrontCartService {

    private final CartFeignClient cartFeignClient;

    // [핵심] 백엔드에서 온 Set-Cookie를 브라우저 응답에 복사
    private void syncCookie(ResponseEntity<?> responseEntity, HttpServletResponse servletResponse) {
        List<String> cookies = responseEntity.getHeaders().get("Set-Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            cookies.forEach(cookie -> servletResponse.addHeader("Set-Cookie", cookie));
        }
    }

    public CartListResponse getCartItems(String cookieHeader) {
        // GET은 쿠키 갱신이 거의 없으므로 Body만 추출
        return cartFeignClient.getCartItems(cookieHeader, null, null).getBody();
    }

    public void addToCart(String cookieHeader, CartAddRequest request, HttpServletResponse servletResponse) {
        ResponseEntity<CartAddResponse> response = cartFeignClient.addItemToCart(request, cookieHeader, null);
        syncCookie(response, servletResponse); // 쿠키 동기화 필수!
    }

    public void updateQuantity(String cookieHeader, CartItemUpdateRequest request) {
        cartFeignClient.updateQuantity(request, cookieHeader, null);
    }

    public void deleteItem(String cookieHeader, Long bookId) {
        cartFeignClient.deleteOneItem(bookId, cookieHeader, null);
    }

    public void clearCart(String cookieHeader) {
        cartFeignClient.deleteAllCartItem(cookieHeader, null);
    }
}