package com.example.high_five.service;

import com.example.high_five.dto.cart.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-server", contextId = "cartClient", url = "localhost:8000")
//@FeignClient(name = "gateway-server", contextId = "memberClient", url = "http://gateway-server:8000")
public interface CartService {

    @PostMapping("/api/cart/items")
    ResponseEntity<CartAddResponse> addItemToCart(
            @RequestBody CartAddRequest request,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );

    @GetMapping("/api/cart")
    ResponseEntity<CartListResponse> getCartItems(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @SpringQueryMap Pageable pageable
    );

    @DeleteMapping("/api/cart/items")
    ResponseEntity<Void> deleteAllCartItem(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );

    @PutMapping("/api/cart/items")
    ResponseEntity<CartUpdateResponse> updateQuantity(
            @RequestBody CartItemUpdateRequest request,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );

    @DeleteMapping("/api/cart/items/{bookId}")
    ResponseEntity<Void> deleteOneItem(
            @PathVariable("bookId") Long bookId,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );

    @PostMapping("/api/cart/merge")
    ResponseEntity<Void> mergeGuestCart(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );

    // [추가] 비회원 장바구니 삭제 요청 (백엔드로)
    @DeleteMapping("/api/cart/guest")
    ResponseEntity<Void> deleteGuestCartOnly(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader
    );
}