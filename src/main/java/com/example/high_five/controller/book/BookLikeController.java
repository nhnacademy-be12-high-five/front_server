package com.example.high_five.controller.book;

import com.example.high_five.common.MemberIdResolver;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookLikeController {

    private final BookClient bookClient;
    private final MemberIdResolver memberIdResolver;

    // 좋아요 상태 조회: GET /api/books/{bookId}/likes/status
    @GetMapping("/{bookId}/likes/status")
    public ResponseEntity<Boolean> likeStatus(@PathVariable Long bookId) {
        Long memberId = memberIdResolver.resolveRequired();
        return bookClient.getLikeStatus(bookId, memberId);
    }

    // 좋아요 토글: POST /api/books/{bookId}/likes
    @PostMapping("/{bookId}/likes")
    public ResponseEntity<Boolean> toggle(@PathVariable Long bookId) {
        Long memberId = memberIdResolver.resolveRequired();
        return bookClient.toggleLike(bookId, memberId);
    }
}
