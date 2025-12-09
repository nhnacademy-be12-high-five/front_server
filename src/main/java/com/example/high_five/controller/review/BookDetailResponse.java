package com.example.high_five.controller.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookDetailResponse {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer price;
    private String pubDate;
    private String image;
    private String description;
}
