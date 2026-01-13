package com.example.high_five.service;

import com.example.high_five.dto.book.BookInfoDto;
import com.example.high_five.dto.book.ParsingDto;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.BookPagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "gateway-server", contextId = "couponBookSearchClient", url = "${gateway.uri}")
public interface BookFeignClient {

    /**
     * Book Server의 SearchController 호출
     * GET /api/search?keyword={keyword}&page={page}&size={size}
     */

    @GetMapping("/api/search")
    BookPagedResponse<BookResponse> searchBooks(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @GetMapping("/api/admin/books/search-api")
    ResponseEntity<BookInfoDto> searchBookByIsbn(@RequestParam("isbn") String isbn);

    @GetMapping("/api/search/rag-search")
    ResponseEntity<List<BookResponse>> getAiRecommendations(@RequestParam("keyword") String keyword);
}