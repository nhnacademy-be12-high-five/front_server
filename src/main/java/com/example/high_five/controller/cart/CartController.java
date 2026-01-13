package com.example.high_five.controller.cart;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.cart.*;
import com.example.high_five.service.BookFeignClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final FrontCartService cartService;
    private final BookFeignClient bookFeignClient;

    // 장바구니 페이지 조회
    @GetMapping("/cart")
    public String viewCartItems(Model model,
                                @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        CartListResponse cartList = cartService.getCartItems(memberId);
        model.addAttribute("cartList", cartList);
        return "order/cart";
    }

    // 장바구니 담기
    @PostMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<String> addItem(@RequestBody CartAddRequest request,
                                          @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                          HttpServletResponse response) {
        cartService.addToCart(memberId, request, response);
        return ResponseEntity.ok("장바구니에 담겼습니다.");
    }

    // 수량 변경
    @PutMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody CartItemUpdateRequest request,
                                               @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        System.out.println("수량 변경 확인됨");
        cartService.updateQuantity(memberId, request);
        return ResponseEntity.ok().build();
    }

    // 단건 삭제
    @DeleteMapping("/cart/items/{bookId}")
    @ResponseBody
    public ResponseEntity<Void> deleteItem(@PathVariable Long bookId,
                                           @RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        cartService.deleteItem(memberId, bookId);
        return ResponseEntity.noContent().build();
    }

    // 전체 삭제
    @DeleteMapping("/cart/items")
    @ResponseBody
    public ResponseEntity<Void> clearCart(@RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        System.out.println("삭제 요청 확인됨");
        cartService.clearCart(memberId);
        return ResponseEntity.noContent().build();
    }

    // 카운트 뱃지
    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Integer> getCartCount(@RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        try {
            CartListResponse response = cartService.getCartItems(memberId);
            if(response == null || response.items() == null) return ResponseEntity.ok(0);
            return ResponseEntity.ok(response.items().size());
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }

    @PostMapping("/cart/merge")
    @ResponseBody
    public ResponseEntity<Void> mergeCart(@RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                          HttpServletResponse response) {
        cartService.mergeCart(memberId, response);

        expireCookie(response, "guestCookie");

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/guest")
    @ResponseBody
    public ResponseEntity<Void> deleteGuestCart(@RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                                HttpServletResponse response) {
        cartService.deleteGuestCart(memberId, response);

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

    @GetMapping("/cart/ai-recommendations")
    @ResponseBody
    public List<BookResponse> getAiRecommendations(@RequestHeader(name = "X-USER-ID", required = false) Long memberId) {
        // 1. 현재 장바구니 목록 조회
        CartListResponse cartList = cartService.getCartItems(memberId);

        if (cartList == null || cartList.items() == null || cartList.items().isEmpty()) {
            return List.of(); // 장바구니가 비어있으면 추천 안함
        }

        Set<Long> inCartBookIds = cartList.items().stream()
                .map(CartDetailResponse::bookId)
                .collect(Collectors.toSet());

        // 2. 책 제목 추출 (최대 5개만 사용하여 키워드 생성)
        String keyword = cartList.items().stream()
                .limit(5)
                .map(CartDetailResponse::title)
                .collect(Collectors.joining(", ")); // "책제목1, 책제목2..." 형태

        log.info("AI 추천 요청 키워드: {}", keyword);

        // 3. AI 검색 API 호출
        try {
            List<BookResponse> rawRecommendations = bookFeignClient.getAiRecommendations(keyword).getBody();

            if (rawRecommendations == null) {
                return List.of();
            }
            // 받아온 추천 목록에서 장바구니에 있는 책은 제거
            return rawRecommendations.stream()
                    .filter(book -> !inCartBookIds.contains(book.bookId())) // 장바구니에 없는 책만 통과
                    .limit(5) // 화면에 보여질 최대 개수 (JS에서 slice하던 것을 여기서 미리 처리해도 됨)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("AI 추천 서비스 호출 실패", e);
            return List.of(); // 오류 시 빈 목록 반환
        }
    }
}