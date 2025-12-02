package com.example.high_five.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointAdminPolicyRequest {
    private Integer signupPoint;
    private Integer reviewPoint;
    private Integer photoPoint;
}
