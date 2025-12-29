package com.example.high_five.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomPage<T> {
    @Setter
    private List<T> content;
    @Setter
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;

}
