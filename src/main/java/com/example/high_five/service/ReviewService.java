package com.example.high_five.service;

import com.example.high_five.common.annotation.LoginRequired;
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

@FeignClient(name = "review-service", contextId = "reviewClient", url = "localhost:8000") // url은 yml로 관리 권장
public interface ReviewService {

    // 리뷰 등록
    @PostMapping(value = "/api/books/{bookId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ReviewCreateResponse createReview(
                                       @PathVariable("bookId") Long bookId,
                                       @RequestPart(value = "request") ReviewCreateRequest request,
                                       @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    // 리뷰 리스트 조회
    @GetMapping("/api/books/{bookId}/reviews")
    Page<BookReviewResponse> getReviews(
            @PathVariable("bookId") Long bookId,
            @SpringQueryMap Pageable pageable
    );

    // 내 리뷰 단건 조회
    @GetMapping("/api/books/{bookId}/reviews/me")
    BookReviewResponse getMyReview(
            @PathVariable("bookId") Long bookId
    );

    // 마이페이지 리뷰 리스트
    @GetMapping("/api/my-page/reviews")
    Page<MyPageReviewResponse> getMyReviews(
            @SpringQueryMap Pageable pageable
    );

    // 리뷰 수정
    @PutMapping(value = "/api/books/{bookId}/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void updateMyReview(
            @PathVariable("bookId") Long bookId,
            @PathVariable("reviewId") Long reviewId,
            @RequestPart(value = "review") ReviewUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    // 6. 리뷰 삭제 (로그인 필요)
//    @LoginRequired
//    @DeleteMapping("/api/reviews/{reviewId}")
//    ResponseEntity<Void> removeReview(
//            @PathVariable("reviewId") Long reviewId
//    );
}