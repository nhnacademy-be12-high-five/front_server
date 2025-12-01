package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCouponResponseDto {
    private Long id;
    private String couponName;
    private String status;
    private Long discountValue;
    private LocalDateTime expiredAt;
    private String condition;
}
