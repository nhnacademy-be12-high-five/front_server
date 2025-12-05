package com.example.high_five.controller.cart;

import com.example.high_five.dto.cart.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final FrontCartService cartService;

    // 장바구니 페이지 조회
    @GetMapping("/api/cart")
    public String viewCartItems(Model model,
                                @RequestHeader(value = "Cookie", required = false) String cookie) {
        CartListResponse cartList = cartService.getCartItems(cookie);
        model.addAttribute("cartList", cartList);
        return "order/cart";
    }

    // 장바구니 담기
    @PostMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<String> addItem(@RequestBody CartAddRequest request,
                                          @RequestHeader(value = "Cookie", required = false) String cookie,
                                          HttpServletResponse response) {
        cartService.addToCart(cookie, request, response);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // 수량 변경
    @PutMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody CartItemUpdateRequest request,
                                               @RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.updateQuantity(cookie, request);
        return ResponseEntity.ok().build();
    }

    // 단건 삭제
    @DeleteMapping("/api/cart/items/{bookId}")
    @ResponseBody
    public ResponseEntity<Void> deleteItem(@PathVariable Long bookId,
                                           @RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.deleteItem(cookie, bookId);
        return ResponseEntity.noContent().build();
    }

    // 전체 삭제
    @DeleteMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<Void> clearCart(@RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.clearCart(cookie);
        return ResponseEntity.noContent().build();
    }

    // 카운트 뱃지
    @GetMapping("/api/cart/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartCount(@RequestHeader(value = "Cookie", required = false) String cookie) {
        try {
            CartListResponse response = cartService.getCartItems(cookie);
            if(response == null || response.items() == null) return ResponseEntity.ok(0);
            return ResponseEntity.ok(response.items().size());
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }
    @GetMapping("/test")
    public String viewTestPage() {
        return "order/test-products";
    }
}