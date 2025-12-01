package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateDto {
    private Long id;
    private Long couponPolicyId;
    private String couponName;
    private String description;
    private Integer issueCount;
    private LocalDateTime issueStartAt;
    private LocalDateTime issueEndAt;
    private Integer validPeriodDate;
    private LocalDateTime validEndAt;
    private Integer remainingCount;
}
