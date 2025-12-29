package com.example.high_five.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResponse {
    private Long orderId;
    private String orderKey;
    private Integer totalAmount;
}