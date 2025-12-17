package com.example.high_five.dto.review;

import java.time.ZonedDateTime;

public record MyPageReviewResponse (Long reviewId,
                                    Long bookId,
                                    String bookTitle,
                                    ZonedDateTime createdAt){
}
