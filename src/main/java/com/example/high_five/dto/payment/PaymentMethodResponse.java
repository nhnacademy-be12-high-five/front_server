package com.example.high_five.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {
    private Long id;
    private String name;    // 시스템 코드 (예: TOSS, CARD)
    private String alias;   // 화면 표시 이름 (예: 토스페이, 신용카드)
    private boolean active;
}