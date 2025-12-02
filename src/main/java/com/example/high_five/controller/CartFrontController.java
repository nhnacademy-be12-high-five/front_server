package com.example.high_five.controller;

import com.example.high_five.cartDto.CartItemUpdateRequest;
import com.example.high_five.cartDto.CartListResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class CartFrontController {

    private final CartClient cartClient;

    @GetMapping
    public String viewCartItems(Model model,
                                @RequestHeader(value = "Cookie", required = false) String cookie) {
        CartListResponse cartList = cartClient.getCartItems(cookie);
        model.addAttribute("cartList", cartList);
        return "order/cart";
    }

    @PutMapping("/items")
    @ResponseBody // HTML이 아니라 데이터(JSON/상태코드)만 돌려줄 때 필수
    public ResponseEntity<Void> updateQuantity(@RequestBody CartItemUpdateRequest request,
                                               @RequestHeader(value = "Cookie", required = false) String cookie) {
        cartClient.updateQuantity(cookie, request);

        return ResponseEntity.ok().build();
    }
}