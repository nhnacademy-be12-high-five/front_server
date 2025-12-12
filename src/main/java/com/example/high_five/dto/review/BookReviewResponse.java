package com.example.high_five.dto.review;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.sql.Timestamp;
import java.util.List;

public record BookReviewResponse(
        @JsonAlias({"id", "reviewId"})
        Long reviewId,
        String loginId,
        String content,
        int rating,
        Timestamp createdAt,
        List<String> imageUrls) {
}
