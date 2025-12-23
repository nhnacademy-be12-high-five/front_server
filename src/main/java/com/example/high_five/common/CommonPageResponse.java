package com.example.high_five.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor // JSON 파싱을 위해 기본 생성자 필요
public class CommonPageResponse<T> {
    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
    private boolean isLast;

    public boolean isFirst() {
        return this.pageNumber == 0;
    }

    public boolean isLast() {
        return this.pageNumber >= this.totalPages - 1;
    }
}