package com.example.high_five.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Data; // 롬복 사용
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Getter, Setter 자동 생성 -> Feign이 아주 좋아함
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    // validation 어노테이션 유지
    @Min(1)
    @Max(5)
    private int rating;
    @NotBlank
    private String content;
}