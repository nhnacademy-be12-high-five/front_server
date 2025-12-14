package com.example.high_five.service;

import ch.qos.logback.core.model.Model;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "book-service", contextId = "booClient", url = "${gateway.uri}")
public interface BookClient {

    // 1. 도서 상세 조회
    @GetMapping("/api/books/{book-id}")
    BookResponse getBookDetail(@PathVariable("book-id") Long id);

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

    // 6. 주간 인기 급상승 로직
    @GetMapping("/api/books/popular")
    List<BookResponse> getPopularBooks();

    @GetMapping("/api/categories/{categoryId}/books")
    List<BookResponse> getBooksByCategory(@PathVariable("categoryId") long categoryId);

    // 도서 좋아요 조회
    @GetMapping("/members/me/likes")
    Boolean getBookLike(@PathVariable("book-id") Long id);

    // 좋아요를 눌렀는데 로그인이 안되어있다면 로그인창으로 리다이렉팅
    @PostMapping("/api/books/{book-id}/likes")
    void toggleLike(@PathVariable("book-id") Long bookId,
                          @RequestHeader(value = "X-USER-ID", required = true) Long memberId);

    @GetMapping("/books/{book-id}/likes/status")
    ResponseEntity<Boolean> getLikeStatus(@PathVariable("book-id") Long bookId,
                                          @RequestHeader(value = "X-USER-ID", required = true) Long memberId);

}