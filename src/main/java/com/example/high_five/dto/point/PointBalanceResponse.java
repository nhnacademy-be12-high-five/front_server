package com.example.high_five.dto.point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointBalanceResponse {
    private Long memberId;
    private Long currentPoint;
    private Long totalEarnedPoint;
}
