package com.example.high_five.dto.book.request;

import lombok.Data;
import java.util.List;

@Data
public class BookAdminUpdateRequest {
    private String title;
    private Integer price;
    private String isbn;
    private String publisher;
    private String publishedDate;
    private String description;
    private String image;
    private List<String> authors;
}