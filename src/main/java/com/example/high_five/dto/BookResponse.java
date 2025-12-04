package com.example.high_five.dto;

public record BookResponse (
        Long id,
        String title,
        String author,
        String isbn,
        Integer price,
        String image,
        Integer categoryId,
        String content,
        String publisher,
        String publishedDate,
        Double avgRating,
        Long reviewCount
) { }
