package com.example.high_five.dto.cart;

public record GetBookResponse(Long bookId,
                              String title,
                              Integer price,
                              String image) {}