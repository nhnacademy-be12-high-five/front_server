package com.example.high_five.service;

import com.example.high_five.dto.review.*;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// name은 유레카 등에 등록된 백엔드 서비스 이름, url은 직접 지정 시 사용
@FeignClient(name = "gateway-server", contextId = "reviewClient", url = "http://localhost:8000")
public interface ReviewService {

    //리뷰 등록
    @PostMapping(value = "/books/{bookId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ReviewCreateResponse> createReview(
            @PathVariable("bookId") Long bookId,
            @RequestPart("request") ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    // 책에 대한 리뷰 리스트 조회
    @GetMapping("/books/{bookId}/reviews")
    ResponseEntity<Page<BookReviewResponse>> getReviews(
            @PathVariable("bookId") Long bookId,
            Pageable pageable
    );

    // 내 리뷰 단건 조회
    @GetMapping("/books/{bookId}/reviews/me")
    ResponseEntity<BookReviewResponse> getMyReview(
            @PathVariable("bookId") Long bookId
    );

    // 마이페이지 내 리뷰 리스트 조회
    @GetMapping("/members/reviews")
    ResponseEntity<Page<MyPageReviewResponse>> getMyReviews(
            Pageable pageable
    );

    // 리뷰 수정
    @PutMapping(value = "/books/{bookId}/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UpdateReviewResponse> updateMyReview(
            @PathVariable("bookId") Long bookId,
            @PathVariable("reviewId") Long reviewId,
            @RequestPart("review") ReviewUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    );

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    ResponseEntity<Void> removeReview(@PathVariable("reviewId") Long reviewId);
}