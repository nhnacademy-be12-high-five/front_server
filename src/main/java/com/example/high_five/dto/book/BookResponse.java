package com.example.high_five.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookResponse {

    private Long id;
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
}
