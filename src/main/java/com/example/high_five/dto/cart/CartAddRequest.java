package com.example.high_five.dto.cart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CartAddRequest(Long bookId, int quantity) {}
