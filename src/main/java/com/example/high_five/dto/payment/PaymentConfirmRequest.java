package com.example.high_five.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

    private String paymentKey;

    private String orderKey;

    private Long amount;

    private String paymentMethod; // 내부 로직 용
}
