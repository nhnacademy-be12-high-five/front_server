package com.example.high_five.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 북서버에서 내려주는 Spring Data Page JSON을 그대로 받기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class PagedResponse<T> {

    // ★ 여기 이름이 꼭 "content" 여야 함
    private List<T> content;

    private int totalPages;
    private long totalElements;
    private int number;            // 현재 페이지 번호(0부터)
    private int size;              // 페이지 크기
    private int numberOfElements;  // 현재 페이지에 담긴 요소 수

    private boolean first;
    private boolean last;
    private boolean empty;
}
