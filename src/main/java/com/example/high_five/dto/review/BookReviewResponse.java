package com.example.high_five.dto.review;

import java.sql.Timestamp;
import java.util.List;

// memberId -> loginId로 바꿔야함
public record BookReviewResponse(String loginId,
                                 String content,
                                 int rating,
                                 Timestamp createdAt,
                                 List<String> imageUrls){}
