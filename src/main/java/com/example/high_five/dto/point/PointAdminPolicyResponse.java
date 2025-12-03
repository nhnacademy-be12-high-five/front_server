package com.example.high_five.dto.point;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointAdminPolicyResponse {
    private Integer signupPoint;
    private Integer reviewPoint;
    private Integer photoPoint;
}