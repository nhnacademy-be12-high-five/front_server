package com.example.high_five.dto.order;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateResponse {
    private Long orderId;
    private String orderKey;
    private Integer totalAmount;
}