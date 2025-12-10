package com.example.high_five.service;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "book-service", contextId = "bookClient", url = "${gateway.uri}")
public interface BookService {

    // 1. 도서 상세 조회
    @GetMapping("/api/books/{id}")
    BookResponse getBookDetail(@PathVariable("id") Long id);

    // 2. 일반 검색
    @GetMapping("/api/search")
    PagedResponse<BookResponse> search(
            @RequestParam("keyword") String keyword,
            @RequestParam("sort") String sort,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 3. AI 검색 (RAG 리스트)
    @GetMapping("/api/search/rag-search")
    PagedResponse<BookResponse> ragSearch(
            @RequestParam("keyword") String keyword,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );

    // 4. AI 답변 (RAG 요약)
    @GetMapping("/api/search/rag-answer")
    String ragAnswer(@RequestParam("keyword") String keyword);

    // 5. 신간 추천 책 리스트
    @GetMapping("/api/books/new")
    List<BookResponse> getNewBooks();
}