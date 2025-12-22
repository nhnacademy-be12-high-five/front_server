package com.example.high_five.service;

import com.example.high_five.dto.book.request.BookAdminUpdateRequest;
import com.example.high_five.dto.book.request.BookRequest;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "book-service", contextId = "bookClient", url = "${gateway.uri}")
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
    List<BookResponse> getNewBooks(@RequestParam("size") int size);

    // 6. 주간 인기 급상승 로직
    @GetMapping("/api/books/popular")
    List<BookResponse> getPopularBooks(@RequestParam("size") int size);


    // 베스트셀러
    @GetMapping("/api/books/best-seller")
    List<BookResponse> getBestSellers(@RequestParam("size") int size);

    // 도서 좋아요 조회
    @GetMapping("/members/me/likes")
    Boolean getBookLike(@PathVariable("book-id") Long id);

    // 좋아요 토글
    @PostMapping("/api/books/{bookId}/likes")
    ResponseEntity<Boolean> toggleLike(
            @PathVariable("bookId") Long bookId,
            @RequestHeader("X-USER-ID") Long memberId
    );

    // 좋아요 상태 조회
    @GetMapping("/api/books/{bookId}/likes/status")
    ResponseEntity<Boolean> getLikeStatus(
            @PathVariable("bookId") Long bookId,
            @RequestHeader("X-USER-ID") Long memberId
    );

    // [관리자] 도서 전체 조회
    @GetMapping("/api/admin/books")
    List<BookResponse> getAdminBooks(@RequestParam(value = "page", defaultValue = "0") int page,
                                     @RequestParam(value = "size", defaultValue = "10") int size);

    // [관리자] 도서 등록
    @PostMapping("/api/admin/books")
    BookResponse createBook(@RequestBody BookRequest bookRequest);

    // [관리자] 도서 수정
    @PutMapping("/api/admin/books/{id}")
    BookResponse updateBook(@PathVariable("id") Long bookId,
                            @RequestBody BookAdminUpdateRequest updateRequest);

    // [관리자] 도서 삭제
//    @DeleteMapping("/api/admin/{id}")
//    void deleteBook(@PathVariable("id") Long bookId,
//                    @RequestHeader("X-User-Id") Long userId);
    @GetMapping("/api/categories/{categoryId}/books")
    List<BookResponse> getBooksByCategory(@PathVariable("categoryId") int categoryId);

}