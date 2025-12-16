package com.example.high_five.dto.order;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderCheckoutRequest {

    private Long userId;
    private String orderPassword;

    private String receiverName;
    private String receiverAddress;

    private String receiverPhone; // 추가하신 필드

    // [수정됨] {} 오타를 private  타입으로 변경
    private LocalDate requestDeliveryDate;

    private Long couponId;
    private Integer usedPoint;

    private List<OrderItemDto> orderItems;


    @Getter
    @Setter
    @ToString
    public static class OrderItemDto {
        private Long bookId;
        private Integer quantity;
        private Long wrapperId;
    }
}