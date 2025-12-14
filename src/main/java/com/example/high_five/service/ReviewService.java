package com.example.high_five.service;

import com.example.high_five.config.FeignMultipartConfig;
import com.example.high_five.dto.review.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap; // Pageable 처리용 권장
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "TEAM5-GATEWAY-SERVER", contextId = "reviewClient", url = "${gateway.uri}", configuration = FeignMultipartConfig.class) // url은 yml로 관리 권장
public interface ReviewService {

    // 리뷰 등록
    @PostMapping(value = "/api/books/{book-id}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ReviewCreateResponse createReview(
            @PathVariable("book-id") Long bookId,
            @RequestPart("request") MultipartFile requestFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    // 리뷰 리스트 조회
    @GetMapping("/api/books/{book-id}/reviews")
    Page<BookReviewResponse> getReviews(
            @PathVariable("book-id") Long bookId,
            @SpringQueryMap Pageable pageable
    );

    // 내 리뷰 단건 조회
    @GetMapping("/api/books/{book-id}/reviews/me")
    BookReviewResponse getMyReview(
            @PathVariable("book-id") Long bookId
    );

    // 마이페이지 리뷰 리스트
    @GetMapping("/api/books/members/me/reviews")
    Page<MyPageReviewResponse> getMyReviews(
            @SpringQueryMap Pageable pageable
    );

    // 리뷰 수정
    @PostMapping(value = "/api/books/{book-id}/reviews/{review-id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void updateMyReview(
            @PathVariable("book-id") Long bookId,
            @PathVariable("review-id") Long reviewId,
            @RequestPart("request") MultipartFile requestFile,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    @PostMapping("/api/books/{book-id}/reviews/{review-id}/like")
    Boolean toggleReviewLike(
            @PathVariable("book-id") Long bookId,
            @PathVariable("review-id") Long reviewId
    );
}

    // 6. 리뷰 삭제 (로그인 필요)
//    @LoginRequired
//    @DeleteMapping("/api/reviews/{reviewId}")
//    ResponseEntity<Void> removeReview(
//            @PathVariable("reviewId") Long reviewId
//    );