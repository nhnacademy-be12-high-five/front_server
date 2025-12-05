package com.example.high_five.controller.review;

import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.dto.review.MyPageReviewResponse;
import com.example.high_five.dto.review.ReviewCreateRequest;
import com.example.high_five.dto.review.ReviewUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final FrontReviewService frontReviewService;

    /**
     * [VIEW] 마이페이지 - 내 리뷰 관리 페이지
     */
    @GetMapping("/mypage/reviews")
    public String viewMyReviews(Model model,
                                @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MyPageReviewResponse> myReviews = frontReviewService.getMyReviews(pageable);
        model.addAttribute("myReviews", myReviews);

        return "/member/mypage";
    }

    /**
     * [AJAX] 책 상세페이지 - 리뷰 리스트 조회 (페이징)
     */
    @GetMapping("/books/{bookId}/reviews")
    public String getReviewList(@PathVariable Long bookId,
                                @PageableDefault(size = 5) Pageable pageable,
                                Model model) {
        // 리뷰 페이징 조회 로직
        Page<BookReviewResponse> reviews = frontReviewService.getBookReviews(bookId, pageable);
        model.addAttribute("reviews", reviews);

        // 중요: "review-fragment"라는 HTML 파일 안의 "reviewList" 조각만 반환
        return "Book/review/review-fragment";
    }

    /**
     * [AJAX] 책 상세페이지 - 내가 쓴 리뷰 단건 조회
     */
    @GetMapping("/api/books/{bookId}/reviews/me")
    @ResponseBody
    public ResponseEntity<BookReviewResponse> getMyReview(@PathVariable Long bookId) {
        BookReviewResponse response = frontReviewService.getMyReview(bookId);
        if (response == null) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(response);
    }

    /**
     * [AJAX - Fragment] 내 리뷰 영역 HTML 반환
     * URL: /books/{bookId}/reviews/me/view
     */
    @GetMapping("/books/{bookId}/reviews/me/view")
    public String getMyReviewFragment(@PathVariable Long bookId,
                                      Model model) {


        if (false) {
            // 비로그인 상태면 null을 넘겨서 '작성하기' 버튼이 뜨게 하거나,
            // 아예 '로그인하세요' 화면을 만들 수도 있음
            model.addAttribute("myReview", null);
        } else {
            BookReviewResponse myReview = frontReviewService.getMyReview(bookId);
            model.addAttribute("myReview", myReview);
        }

        return "/Book/review/my-review";
    }

    /**
     * [AJAX] 리뷰 등록
     * 주의: JS에서 FormData로 'request'(JSON Blob)와 'images'(File)를 보내야 함
     */
    @PostMapping(value = "/api/books/{bookId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> createReview(@PathVariable Long bookId,
                                               @RequestPart("request") ReviewCreateRequest request,
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        frontReviewService.createReview(bookId, request, images);
        return ResponseEntity.status(201).body("리뷰가 등록되었습니다.");
    }

    /**
     * [AJAX] 리뷰 수정
     */
    @PutMapping(value = "/api/books/{bookId}/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> updateReview(@PathVariable Long bookId,
                                               @PathVariable Long reviewId,
                                               @RequestPart("review") ReviewUpdateRequest request, // Feign Client 파라미터명과 일치 ('review')
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        frontReviewService.updateReview(bookId, reviewId, request, images);
        return ResponseEntity.ok("리뷰가 수정되었습니다.");
    }

    /**
     * [AJAX] 리뷰 삭제
     */
    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        frontReviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}