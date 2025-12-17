package com.example.high_five.dto.book.response;

import com.example.high_five.dto.Tag.response.TagResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BookResponse {

    @JsonProperty("id")
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private Integer price;
    private String image;
    private Integer categoryId;
    private String content;
    private String publisher;
    private String publishedDate;
    private Double avgRating;
    private Long reviewCount;

    private String aiSummary;
    private String aiReviewSummary;

    // book-detail.html 에서 사용하는 태그 필드
    private List<TagResponse> tags;
}