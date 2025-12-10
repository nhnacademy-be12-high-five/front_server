package com.example.high_five.service;

import com.example.high_five.dto.book.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "TEAM5-GATEWAY-SERVER", contextId = "bookClient", url = "${gateway.uri}")
public interface BookService {

    // 도서 상세 조회
    @GetMapping("/api/books/{book-id}")
    BookResponse getBookDetail(@PathVariable("book-id") Long id);

    // 일반 검색
    @GetMapping("/api/search")
    PagedResponse<BookResponse> search(
            @RequestParam("keyword") String keyword,
            @RequestParam("sort") String sort,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // AI 검색 (RAG 리스트)
    @GetMapping("/api/search/rag-search")
    PagedResponse<BookResponse> ragSearch(
            @RequestParam("keyword") String keyword,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // AI 답변 (RAG 요약)
    @GetMapping("/api/search/rag-answer")
    String ragAnswer(@RequestParam("keyword") String keyword);
}