package com.example.high_five.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;
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
        List<ReviewImageResponse> reviewImages,

        Integer likeCount,
        boolean isLiked) {
}