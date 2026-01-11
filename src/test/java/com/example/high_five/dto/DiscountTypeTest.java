package com.example.high_five.dto;

import com.example.high_five.dto.coupon.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountTypeTest {

    @Test
    @DisplayName("DiscountType 정의된 상수 값 확인")
    void enumConstants() {
        // then
        assertThat(DiscountType.FIXED).isNotNull();
        assertThat(DiscountType.PERCENTAGE).isNotNull();
        
        assertThat(DiscountType.FIXED.name()).isEqualTo("FIXED");
        assertThat(DiscountType.PERCENTAGE.name()).isEqualTo("PERCENTAGE");
    }

    @Test
    @DisplayName("valueOf()를 통한 조회 테스트")
    void valueOfTest() {
        // when & then
        assertThat(DiscountType.valueOf("FIXED")).isEqualTo(DiscountType.FIXED);
        assertThat(DiscountType.valueOf("PERCENTAGE")).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    @DisplayName("정의되지 않은 상수 조회 시 예외 발생")
    void invalidConstant() {
        // when & then
        assertThatThrownBy(() -> DiscountType.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 상수 개수 확인")
    void valuesSize() {
        // given
        DiscountType[] values = DiscountType.values();

        // then
        // 현재 정의된 타입은 FIXED, PERCENTAGE 2개
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(DiscountType.FIXED, DiscountType.PERCENTAGE);
    }
}