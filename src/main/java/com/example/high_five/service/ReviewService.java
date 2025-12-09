package com.example.high_five.service;

import com.example.high_five.dto.review.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap; // Pageable 처리용 권장
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "gateway-server", contextId = "reviewClient", url = "http://localhost:8000")
public interface ReviewService {

    // 1. 리뷰 등록 (로그인 필요 -> 쿠키 필수)
    @PostMapping(value = "/api/books/{bookId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ReviewCreateResponse> createReview(
            @PathVariable("bookId") Long bookId,
            @RequestPart("request") ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );

    // 2. 책 리뷰 리스트 조회 (비로그인도 가능하지만, '좋아요' 여부 등을 위해 쿠키 전달 권장)
    @GetMapping("/api/books/{bookId}/reviews")
    ResponseEntity<Page<BookReviewResponse>> getReviews(
            @PathVariable("bookId") Long bookId,
            @SpringQueryMap Pageable pageable, // [Tip] Feign에서 Pageable 쓸 땐 이게 안전함
            @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );

    // 3. 내 리뷰 단건 조회 (로그인 필요)
    @GetMapping("/api/books/{bookId}/reviews/me")
    ResponseEntity<BookReviewResponse> getMyReview(
            @PathVariable("bookId") Long bookId,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );

    // 4. 마이페이지 내 리뷰 리스트 (로그인 필요)
    @GetMapping("/api/my-page/reviews")
    ResponseEntity<Page<MyPageReviewResponse>> getMyReviews(
            @SpringQueryMap Pageable pageable,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );

    // 5. 리뷰 수정 (로그인 필요)
    @PutMapping(value = "/api/books/{bookId}/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Void> updateMyReview( // 반환 타입 Void로 단순화 가능
                                         @PathVariable("bookId") Long bookId,
                                         @PathVariable("reviewId") Long reviewId,
                                         @RequestPart("review") ReviewUpdateRequest request,
                                         @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                         @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );

    // 6. 리뷰 삭제 (로그인 필요)
    @DeleteMapping("/api/reviews/{reviewId}")
    ResponseEntity<Void> removeReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestHeader(value = "Cookie", required = false) String cookieHeader // [추가]
    );
}