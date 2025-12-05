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
// 화면 주소와 API 주소를 구분하기 위해 루트는 /cart 로 잡거나,
// 아래처럼 메서드별로 나누는 게 좋습니다. 여기서는 사용자가 준 코드 기반으로 작성합니다.
public class CartController {

    private final FrontCartService cartService;

    // 1. 장바구니 페이지 조회 (HTML 반환)
    @GetMapping("/api/cart")
    public String viewCartItems(Model model,
                                @RequestHeader(value = "Cookie", required = false) String cookie) {
        CartListResponse cartList = cartService.getCartItems(cookie);
        model.addAttribute("cartList", cartList);
        return "order/cart"; // templates/order/cart.html
    }

    // 2. 장바구니 담기 (AJAX) - /api/cart/items
    @PostMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<String> addItem(@RequestBody CartAddRequest request,
                                          @RequestHeader(value = "Cookie", required = false) String cookie,
                                          HttpServletResponse response) {
        cartService.addToCart(cookie, request, response);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // 3. 수량 변경 (AJAX)
    @PutMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody CartItemUpdateRequest request,
                                               @RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.updateQuantity(cookie, request);
        return ResponseEntity.ok().build();
    }

    // 4. 단건 삭제 (AJAX)
    @DeleteMapping("/api/cart/items/{bookId}")
    @ResponseBody
    public ResponseEntity<Void> deleteItem(@PathVariable Long bookId,
                                           @RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.deleteItem(cookie, bookId);
        return ResponseEntity.noContent().build();
    }

    // 5. 전체 삭제 (AJAX)
    @DeleteMapping("/api/cart/items")
    @ResponseBody
    public ResponseEntity<Void> clearCart(@RequestHeader(value = "Cookie", required = false) String cookie) {
        cartService.clearCart(cookie);
        return ResponseEntity.noContent().build();
    }

    // 6. 카운트 조회 (뱃지용) - 필요 시 서비스에 메서드 추가 후 사용
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
        return "order/test-products"; // templates/order/test-products.html
    }
}