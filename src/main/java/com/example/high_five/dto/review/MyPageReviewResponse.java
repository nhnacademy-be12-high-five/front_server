package com.example.high_five.dto.review;

import java.sql.Timestamp;

public record MyPageReviewResponse (Long reviewId,
                                    Long bookId,
                                    String bookTitle,
                                    Timestamp createdAt){
}
