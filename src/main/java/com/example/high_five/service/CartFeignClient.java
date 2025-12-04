package com.example.high_five.service;

import com.example.high_five.dto.cart.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

// Member-Server의 이름과 경로 확인 (Eureka 이름: member-service)
@FeignClient(name = "gateway-server", contextId = "cartClient", url = "http://localhost:8082")
public interface CartFeignClient {

    @PostMapping("/api/cart/items")
    ResponseEntity<CartAddResponse> addItemToCart(
            @RequestBody CartAddRequest request,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "Authorization", required = false) String accessToken
    );

    @GetMapping("/api/cart")
    ResponseEntity<CartListResponse> getCartItems(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @SpringQueryMap Pageable pageable
    );

    @DeleteMapping("/api/cart/items")
    ResponseEntity<Void> deleteAllCartItem(
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "Authorization", required = false) String accessToken
    );

    @PutMapping("/api/cart/items")
    ResponseEntity<CartUpdateResponse> updateQuantity(
            @RequestBody CartItemUpdateRequest request,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "Authorization", required = false) String accessToken
    );

    @DeleteMapping("/api/cart/items/{bookId}")
    ResponseEntity<Void> deleteOneItem(
            @PathVariable("bookId") Long bookId,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader,
            @RequestHeader(value = "Authorization", required = false) String accessToken
    );

    // 장바구니 개수 조회 (뱃지용)
    // Member Controller에 이 메서드가 없다면 추가해야 합니다. (우리가 나중에 논의한 로직)
    // 없으면 일단 생략하거나 Member 쪽에 추가 필요
}