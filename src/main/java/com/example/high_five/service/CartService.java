package com.example.high_five.service;

import com.example.high_five.dto.cart.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-server", contextId = "cartClient", url = "${gateway.uri}")
//@FeignClient(name = "gateway-server", contextId = "memberClient", url = "http://gateway-server:8000")
public interface CartService {

    @PostMapping("/api/cart/items")
    ResponseEntity<CartAddResponse> addItemToCart(
            @RequestBody CartAddRequest request,
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );

    @GetMapping("/api/cart")
    ResponseEntity<CartListResponse> getCartItems(
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId,
            @SpringQueryMap Pageable pageable
    );

    @PostMapping("/api/cart/items")
    ResponseEntity<Void> deleteAllCartItem(
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );

    @PostMapping("/api/cart/items")
    ResponseEntity<CartUpdateResponse> updateQuantity(
            @RequestBody CartItemUpdateRequest request,
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );

    @PostMapping("/api/cart/items/{bookId}")
    ResponseEntity<Void> deleteOneItem(
            @PathVariable("bookId") Long bookId,
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );

    @PostMapping("/api/cart/merge")
    ResponseEntity<Void> mergeGuestCart(
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );

    // [추가] 비회원 장바구니 삭제 요청 (백엔드로)
    @PostMapping("/api/cart/guest")
    ResponseEntity<Void> deleteGuestCartOnly(
            @RequestHeader(name = "X-USER-ID", required = false) Long memberId,
            @RequestHeader(name = "X-GUEST-ID", required = false) String guestId
    );
}