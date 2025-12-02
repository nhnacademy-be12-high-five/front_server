package com.example.high_five.dto.coupon;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {
    private Long id;

    private Long amount;

    private String description;

    private Long currentPoint;

    private LocalDateTime transactionDate;

    private Long orderId;
}
