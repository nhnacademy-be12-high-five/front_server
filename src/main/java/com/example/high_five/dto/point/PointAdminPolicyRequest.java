package com.example.high_five.dto.point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointAdminPolicyRequest {
    private Integer signupPoint;
    private Integer reviewPoint;
    private Integer photoPoint;
}
