package com.example.high_five.dto.cart;

public record CartDetailResponse(Long bookId,
                                 String title,
                                 Integer price,
                                 int quantity,
                                 Integer totalPrice,
                                 String image) {}
// 제목 저자 가격 사진 수량 총가격
