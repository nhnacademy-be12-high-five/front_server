package com.example.high_five.dto.book.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;


public record BookResponse(
        @JsonProperty("id") Long bookId,
        String title,
        String author,
        String isbn,
        Integer price,
        String image,
        // 북 서버와 필드명, 타입을 일치시켜야 함
        List<CategoryResponse> categories,
        List<TagResponse> tags,
        String content,
        String publisher,
        String publishedDate,
        Double avgRating,
        Long reviewCount,
        String aiSummary,
        String aiReviewSummary
) {
    // 북 서버의 구조와 100% 일치해야 Jackson이 데이터를 담아줌
    public record CategoryResponse(
            Integer categoryId,
            String categoryName
    ) {}


    public record TagResponse(Long tagId,String name){}
}