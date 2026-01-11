package com.example.high_five.dto;

import com.example.high_five.dto.coupon.CouponCreateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CouponCreateRequestDtoTest {

    @Test
    @DisplayName("CouponCreateRequestDto 생성 및 필드 값 확인")
    void createAndCheckFields() {
        // given
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);
        
        // AllArgsConstructor 테스트
        CouponCreateRequestDto dto = new CouponCreateRequestDto(
                1L, "할인쿠폰", "설명", 100,
                start, end, 30, "FIXED"
        );

        // then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCouponName()).isEqualTo("할인쿠폰");
        assertThat(dto.getIssueCount()).isEqualTo(100);
        assertThat(dto.getIssueStartAt()).isEqualTo(start);
        assertThat(dto.getCouponType()).isEqualTo("FIXED");
    }

    @Test
    @DisplayName("NoArgsConstructor 및 Setter 테스트")
    void setterTest() {
        // given
        CouponCreateRequestDto dto = new CouponCreateRequestDto();

        // when
        dto.setCouponName("New Coupon");
        dto.setIssueCount(50);

        // then
        assertThat(dto.getCouponName()).isEqualTo("New Coupon");
        assertThat(dto.getIssueCount()).isEqualTo(50);
    }
}