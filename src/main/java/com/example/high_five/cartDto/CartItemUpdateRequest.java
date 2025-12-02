package com.example.high_five.cartDto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemUpdateRequest(@NotNull Long bookId, @Min(1) int quantity) {}

