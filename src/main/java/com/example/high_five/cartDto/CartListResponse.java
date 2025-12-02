package com.example.high_five.cartDto;

import java.util.List;

public record CartListResponse(
        List<CartDetailResponse> items,
        long totalCartPrice // 전체 총 주문 금액
) {}