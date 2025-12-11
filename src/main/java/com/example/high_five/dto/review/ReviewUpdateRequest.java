package com.example.high_five.dto.review;

import java.util.List;

public record ReviewUpdateRequest(String content,
                                  Integer rating,
                                  List<Long> deleteImageIds) {}