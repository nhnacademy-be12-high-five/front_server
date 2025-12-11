package com.example.high_five.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponse {
    private String paymentId;

    private PaymentStatus status;

    private Long amount;
}
