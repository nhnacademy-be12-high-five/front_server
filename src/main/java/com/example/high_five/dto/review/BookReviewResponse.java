package com.example.high_five.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public record BookReviewResponse(
        @JsonAlias({"id", "reviewId"})
        Long reviewId,
        Long memberId,
        String loginId,
        String content,
        int rating,
        ZonedDateTime createdAt,
        List<String> imageUrls,
        Integer likeCount,
        boolean isLiked) {
}
