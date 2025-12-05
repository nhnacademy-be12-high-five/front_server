package com.example.high_five.dto.coupon;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CouponPolicyResponseDto {
    private Long id;
    private String name;
    private String comment;
    private String discountType;
    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountValue;
    private String status;
    private List<Long> targetBookIds;
    private List<Long> targetCategoryIds;
}
