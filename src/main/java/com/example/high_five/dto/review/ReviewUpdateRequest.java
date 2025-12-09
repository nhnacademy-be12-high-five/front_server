package com.example.high_five.dto.review;

import java.util.List;

public record ReviewUpdateRequest(String content,
                                  int rating,
                                  List<Long> deleteImageIds) {}
