package com.example.high_five.dto.book.response;

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

    // book-detail.html 에서 사용하는 태그 필드
    private List<String> tags;
}