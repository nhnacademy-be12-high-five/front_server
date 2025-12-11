package com.example.high_five.controller.review;

import com.example.high_five.dto.review.*;
import com.example.high_five.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 리뷰 등록
    // HTML form: name="score", name="content" -> request 객체에 자동 매핑
    // HTML form: name="images" -> images 리스트에 매핑
    @PostMapping("/books/{book-id}")
    public String addReview(
            @PathVariable("book-id") Long bookId,
            @ModelAttribute ReviewCreateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.createReview(bookId, request, images);
        return "redirect:/books/" + bookId;
    }

    // 2. 리뷰 리스트 조회 (더보기/페이지네이션용 AJAX)
    @GetMapping("/books/{book-id}")
    @ResponseBody
    public ResponseEntity<Page<BookReviewResponse>> getBookReviewList(
            @PathVariable("book-id") Long bookId,
            @PageableDefault(size = 5) Pageable pageable
    ) {
        Page<BookReviewResponse> reviews = reviewService.getReviews(bookId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 3. 리뷰 수정 폼으로 이동 (HTML의 '수정' 버튼 링크와 매핑)
    @GetMapping("/books/{book-id}/update")
    public String updateReviewForm(
            @PathVariable("book-id") Long bookId,
            Model model
    ) {
        BookReviewResponse myReview = reviewService.getMyReview(bookId);
        model.addAttribute("review", myReview);
        model.addAttribute("bookId", bookId);
        return "review/review-update-form";
    }

    // 4. 리뷰 수정 처리
    @PostMapping("/books/{book-id}/{review-id}/update")
    public String modifyReview(
            @PathVariable("book-id") Long bookId,
            @PathVariable("review-id") Long reviewId,
            @ModelAttribute ReviewUpdateRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        reviewService.updateMyReview(bookId, reviewId, request, images);
        return "redirect:/books/" + bookId;
    }

    // ★ 5. 리뷰 삭제 처리 (이게 빠져있어서 추가했습니다!)
    // HTML: th:action="@{.../delete}" method="post" 와 매핑됨
//    @PostMapping("/books/{book-id}/{review-id}/delete")
//    public String removeReview(
//            @PathVariable("book-id") Long bookId,
//            @PathVariable("review-id") Long reviewId
//    ) {
//        reviewService.deleteMyReview(bookId, reviewId);
//        return "redirect:/books/" + bookId;
//    }

    // 6. 마이페이지 리뷰 관리
    @GetMapping("/me")
    public String myReviewList(
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {
        Page<MyPageReviewResponse> myReviews = reviewService.getMyReviews(pageable);
        model.addAttribute("reviews", myReviews);
        return "mypage/reviews";
    }
}