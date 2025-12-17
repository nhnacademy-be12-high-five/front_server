package com.example.high_five.controller.review;

import com.example.high_five.dto.review.*;
import com.example.high_five.service.ReviewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 등록
    @PostMapping("/books/{book-id}/reviews")
    public String addReview(
            @PathVariable("book-id") Long bookId,
            @ModelAttribute ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        if (images == null) {
            images = List.of();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonBytes;
        try {
            jsonBytes = objectMapper.writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Review Create DTO 변환 실패", e);
        }

        MultipartFile requestFile = new DtoMultipartFile("request", "request.json", "application/json", jsonBytes);

        reviewService.createReview(bookId, requestFile, images);

        return "redirect:/books/" + bookId;
    }

    // 리뷰 리스트 조회
    @GetMapping("/books/{book-id}/reviews")
    @ResponseBody
    public ResponseEntity<Page<BookReviewResponse>> getBookReviewList(
            @PathVariable("book-id") Long bookId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<BookReviewResponse> reviews = reviewService.getReviews(bookId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 수정 폼
    @GetMapping("/books/{book-id}/reviews/update")
    public String updateReviewForm(
            @PathVariable("book-id") Long bookId,
            Model model
    ) {
        BookReviewResponse myReview = reviewService.getMyReview(bookId);
        model.addAttribute("review", myReview);
        model.addAttribute("bookId", bookId);
        return "review/review-update-form";
    }

    // 리뷰 수정 처리
    @PostMapping(value = "/books/{book-id}/reviews/{review-id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateReviewPut(
            @PathVariable("book-id") Long bookId,
            @PathVariable("review-id") Long reviewId,
            @RequestPart("request") ReviewUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        if (images == null) images = List.of();

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] jsonBytes;
        try {
            jsonBytes = objectMapper.writeValueAsBytes(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MultipartFile requestFile = new DtoMultipartFile("request", "request.json", "application/json", jsonBytes);

        reviewService.updateMyReview(bookId, reviewId, requestFile, images);

        return ResponseEntity.ok().build();
    }

    // 마이페이지 리뷰 관리
    @GetMapping("/mypage/reviews")
    public String myReviews(Model model,
                            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
    {
//        if (user == null) {
//            return "redirect:/member/login";
//        }

        // 1. FeignClient로 내 리뷰 조회
        Page<MyPageReviewResponse> reviewPage = reviewService.getMyReviews(pageable);

        // 2. 모델에 담기
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("page", reviewPage); // 페이징 버튼용

        return "mypage/reviews"; // HTML 파일 경로
    }

    // 리뷰 좋아요 요청 처리
    @PostMapping("/books/{book-id}/reviews/{review-id}/like")
    @ResponseBody
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable("book-id") Long bookId,
            @PathVariable("review-id") Long reviewId
    ) {
        Boolean isLiked = reviewService.toggleReviewLike(bookId, reviewId);

        return ResponseEntity.ok(isLiked);
    }
}




// ★ 리뷰 삭제 처리
// HTML: th:action="@{.../delete}" method="post" 와 매핑됨
//    @PostMapping("/books/{book-id}/{review-id}/delete")
//    public String removeReview(
//            @PathVariable("book-id") Long bookId,
//            @PathVariable("review-id") Long reviewId
//    ) {
//        reviewService.deleteMyReview(bookId, reviewId);
//        return "redirect:/books/" + bookId;
//    }