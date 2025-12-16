package com.example.high_five.dto.order;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeliveryPolicyResponse {
    private Long id;
    private Integer standardShippingFee;
    private Integer minOrderAmount;
    private Boolean isActive;
    private LocalDateTime effectiveDate;
    private Integer remoteAreaSurcharge;

}
