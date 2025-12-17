package com.example.high_five.dto.order;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyOrderResponse {

    private Long orderId;
    private LocalDateTime orderDate;
    private String status;
    private Integer totalAmount;
    private List<MyOrderItemResponse> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyOrderItemResponse {
        private String bookTitle;
        private Integer quantity;
        private Integer price;
    }
}