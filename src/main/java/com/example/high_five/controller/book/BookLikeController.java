package com.example.high_five.controller.book;

import com.example.high_five.common.MemberIdResolver;
import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookLikeController {

    private final BookClient bookClient;

    // 좋아요 상태 조회: GET /api/books/{bookId}/likes/status
    @LoginRequired
    @GetMapping("/{book-id}/likes/status")
    public ResponseEntity<Boolean> likeStatus(@PathVariable("book-id") Long bookId) {
        return bookClient.getLikeStatus(bookId);
    }

    // 좋아요 토글: POST /api/books/{bookId}/likes
    @LoginRequired
    @PostMapping("/{book-id}/likes")
    public ResponseEntity<Boolean> toggle(@PathVariable("book-id") Long bookId) {
        return bookClient.toggleLike(bookId);
    }
}
