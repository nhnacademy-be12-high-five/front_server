package com.example.high_five.dto;

import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.order.OrderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseTest {

    @Test
    @DisplayName("OrderResponse Builder 및 중첩 클래스 생성 테스트")
    void builderTest() {
        // given
        OrderResponse.OrderItem item = OrderResponse.OrderItem.builder()
                .bookId(10L)
                .title("Java Basic")
                .price(20000)
                .quantity(1)
                .build();

        OrderResponse.WrapperDto wrapper = OrderResponse.WrapperDto.builder()
                .id(1L)
                .name("Gift Box")
                .price(1000)
                .build();
        
        LocalDateTime now = LocalDateTime.now();

        // when
        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .orderName("Order #1")
                .totalPrice(21000)
                .orderDate(now)
                .orderItems(List.of(item))
                .wrappers(List.of(wrapper))
                .coupons(List.of()) // 빈 리스트
                .build();

        // then
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getOrderName()).isEqualTo("Order #1");
        assertThat(response.getOrderDate()).isEqualTo(now);
        
        // 중첩 객체 리스트 검증
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getOrderItems().get(0).getTitle()).isEqualTo("Java Basic");
        
        assertThat(response.getWrappers()).hasSize(1);
        assertThat(response.getWrappers().get(0).getName()).isEqualTo("Gift Box");
    }

    @Test
    @DisplayName("OrderItem 내부 필드 확인")
    void orderItemTest() {
        OrderResponse.OrderItem item = new OrderResponse.OrderItem(1L, "Title", "img.jpg", 1000, 2, 2000);
        
        assertThat(item.getBookId()).isEqualTo(1L);
        assertThat(item.getTotalPrice()).isEqualTo(2000);
    }
}