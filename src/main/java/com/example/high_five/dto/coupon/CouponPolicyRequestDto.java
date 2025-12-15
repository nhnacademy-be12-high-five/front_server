package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponPolicyRequestDto {
    private String name;
    private String comment;
    private String discountType;
    private Long discountValue;
    private Long minOrderValue;
    private Long maxDiscountValue;
    private Integer validPeriodDate;
    private List<Long> targetCategoryIds = new ArrayList<>();
    private List<Long> targetBookIds = new ArrayList<>();
}
