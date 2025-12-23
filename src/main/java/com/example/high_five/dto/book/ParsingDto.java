package com.example.high_five.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParsingDto {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String pubDate;
    private String price;
    private String imageUrl;
    private String description;
}
