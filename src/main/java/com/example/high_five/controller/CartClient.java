package com.example.high_five.controller;

import com.example.high_five.cartDto.CartItemUpdateRequest;
import com.example.high_five.cartDto.CartListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "GATEWAY", url = "http://localhost:8082")
public interface CartClient {

    @GetMapping("/api/cart")
    CartListResponse getCartItems(@RequestHeader("Cookie") String cookie);

    @PutMapping("/api/cart/items")
    ResponseEntity<Void> updateQuantity(
            @RequestHeader("Cookie") String cookie,
            @RequestBody CartItemUpdateRequest request
    );
}
