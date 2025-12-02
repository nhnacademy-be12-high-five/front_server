package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointAdminAdjustmentRequest {
    private Long memberId;

    private Long amount;

    private String reason;
}
