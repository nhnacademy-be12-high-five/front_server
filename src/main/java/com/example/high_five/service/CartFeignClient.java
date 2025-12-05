package com.example.high_five.service;

import com.example.high_five.dto.cart.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;

@FeignClient(name = "gateway-server", contextId = "cartClient", url = "${gateway.uri}")
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
}