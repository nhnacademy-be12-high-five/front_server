package com.example.high_five.controller.cart;

import com.example.high_five.dto.cart.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final FrontCartService cartService;

    // 장바구니 페이지 조회
    @GetMapping("/cart")
    public String viewCartItems(Model model,
                                @CookieValue(value = "guestCookie", required = false) String guestId,
                                @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        CartListResponse cartList = cartService.getCartItems(memberId, guestId);
        model.addAttribute("cartList", cartList);
        return "order/cart";
    }

    // 장바구니 담기
    @PostMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<String> addItem(@RequestBody CartAddRequest request,
                                          @CookieValue(value = "guestCookie", required = false) String guestId,
                                          @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                          HttpServletResponse response) {
        cartService.addToCart(memberId, guestId, request, response);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // 수량 변경
    @PutMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody CartItemUpdateRequest request,
                                               @CookieValue(value = "guestCookie", required = false) String guestId,
                                               @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        log.info(">>> cart update start");
        cartService.updateQuantity(memberId, guestId, request);
        log.info("<<< cart update end");
        return ResponseEntity.ok().build();
    }

    // 단건 삭제
    @DeleteMapping("/cart/items/{bookId}")
    @ResponseBody
    public ResponseEntity<String> deleteItem(@PathVariable Long bookId,
                                           @CookieValue(value = "guestCookie", required = false) String guestId,
                                           @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        return ResponseEntity.ok("front-ok");
    }

    // 전체 삭제
    @DeleteMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<Void> clearCart(@CookieValue(value = "guestCookie", required = false) String guestId,
                                          @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        cartService.clearCart(memberId, guestId);
        return ResponseEntity.noContent().build();
    }

    // 카운트 뱃지
    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartCount(@CookieValue(value = "guestCookie", required = false) String guestId,
                                                @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        try {
            CartListResponse response = cartService.getCartItems(memberId, guestId);
            if(response == null || response.items() == null) return ResponseEntity.ok(0);
            return ResponseEntity.ok(response.items().size());
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    @PostMapping("/cart/merge")
    @ResponseBody
    public ResponseEntity<Void> mergeCart(@CookieValue(value = "guestCookie", required = false) String guestId,
                                          @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                          HttpServletResponse response) {
        cartService.mergeCart(memberId, guestId, response);

        expireCookie(response, "guestCookie");

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/guest")
    @ResponseBody
    public ResponseEntity<Void> deleteGuestCart(@CookieValue(value = "guestCookie", required = false) String guestId,
                                                @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                                HttpServletResponse response) {
        cartService.deleteGuestCart(memberId, guestId, response);

        expireCookie(response, "guestCookie");

        return ResponseEntity.ok().build();
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 수명 0 = 즉시 삭제
        response.addCookie(cookie);
    }

    @GetMapping("/bookTest")
    public String viewTestPage() {
        return "order/test-products";
    }
}