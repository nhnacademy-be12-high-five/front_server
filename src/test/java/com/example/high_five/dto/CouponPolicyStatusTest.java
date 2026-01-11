package com.example.high_five.dto;

import com.example.high_five.dto.coupon.CouponPolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponPolicyStatusTest {

    @Test
    @DisplayName("CouponPolicyStatus 정의된 상수 값 확인")
    void enumConstants() {
        // then
        assertThat(CouponPolicyStatus.ACTIVE).isNotNull();
        assertThat(CouponPolicyStatus.INACTIVE).isNotNull();
        
        assertThat(CouponPolicyStatus.ACTIVE.name()).isEqualTo("ACTIVE");
        assertThat(CouponPolicyStatus.INACTIVE.name()).isEqualTo("INACTIVE");
    }

    @Test
    @DisplayName("valueOf()를 통한 조회 테스트")
    void valueOfTest() {
        // when & then
        assertThat(CouponPolicyStatus.valueOf("ACTIVE")).isEqualTo(CouponPolicyStatus.ACTIVE);
        assertThat(CouponPolicyStatus.valueOf("INACTIVE")).isEqualTo(CouponPolicyStatus.INACTIVE);
    }

    @Test
    @DisplayName("정의되지 않은 상수 조회 시 예외 발생")
    void invalidConstant() {
        // when & then
        assertThatThrownBy(() -> CouponPolicyStatus.valueOf("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("모든 상수 개수 확인")
    void valuesSize() {
        // given
        CouponPolicyStatus[] values = CouponPolicyStatus.values();

        // then
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(CouponPolicyStatus.ACTIVE, CouponPolicyStatus.INACTIVE);
    }
}