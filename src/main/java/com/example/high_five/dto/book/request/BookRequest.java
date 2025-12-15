package com.example.high_five.dto.book.request;

import lombok.Data;

@Data
public class BookRequest {
    private String seqNo;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String pubDate;
    private String price;
    private String imageUrl;
    private String description;
}
