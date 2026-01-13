package com.example.high_five.dto.book;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoDto {
    private String isbn;
    private String title;
    private List<String> authors;
    private String publisher;
    private String publishedDate;
    private Integer price;
    private String image;
    private String description;
    private Integer categoryId;
}
