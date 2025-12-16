package com.example.high_five.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {
    private Long totalSalesAmount;      // 총 매출
    private Long totalCancelAmount;     // 환불 금액
    private Long netSalesAmount;        // 순 매출
    private Long totalTransactionCount; // 총 결제 건수
    private Long successCount;          // 성공 건수
    private Long cancelCount;           // 취소 건수
}