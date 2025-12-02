package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCouponIssueRequestDto {
    private Long userId;
    private Long couponId;
}
