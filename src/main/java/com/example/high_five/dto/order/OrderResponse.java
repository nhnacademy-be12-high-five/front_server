package com.example.high_five.dto.order;

import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private String name;
    private String phoneNumber;
    private String email;
    private Integer myPoint;

    private List<MemberCouponResponseDto> coupons;
    private List<OrderItem> orderItems;
    private List<WrapperDto> wrappers;

    private int totalProductPrice;
    private int deliveryFee;
    private DeliveryPolicyResponse deliveryPolicy;


    @Getter
    @Builder
    public static class OrderItem {
        private Long bookId;
        private String title;
        private String imageUrl;
        private Integer price;
        private Integer quantity;
        private Integer totalPrice;
    }

    @Getter
    @Builder
    public static class WrapperDto {
        private Long id;
        private String name;
        private Integer price;
    }
}