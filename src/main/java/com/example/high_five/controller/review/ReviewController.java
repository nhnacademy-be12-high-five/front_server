package com.example.high_five.controller.review;

import com.example.high_five.dto.review.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final FrontReviewService frontReviewService;

    // [View] 마이페이지 - 내 리뷰 관리
    @GetMapping("/mypage/reviews")
    public String viewMyReviews(Model model,
                                @PageableDefault(size = 10) Pageable pageable,
                                @RequestHeader(value = "Cookie", required = false) String cookie) {

        // 서비스에서 쿠키를 헤더에 실어 백엔드로 요청
        Page<MyPageReviewResponse> myReviews = frontReviewService.getMyReviews(pageable, cookie);
        model.addAttribute("myReviews", myReviews);

        return "mypage/mypage"; // 경로 확인!
    }

    // [Fragment] 책 상세페이지 - 리뷰 리스트 (페이징)
    @GetMapping("/books/{bookId}/reviews")
    public String getReviewList(@PathVariable Long bookId,
                                @PageableDefault(size = 5) Pageable pageable,
                                Model model) {

        Page<BookReviewResponse> reviews = frontReviewService.getBookReviews(bookId, pageable);
        model.addAttribute("reviews", reviews);

        // 실제 파일 경로: resources/templates/Book/review/review-fragment.html 인지 확인
        return "Book/review/review-fragment";
    }

    // [Fragment] 책 상세페이지 - 내 리뷰 영역 (로그인 체크 포함)
    @GetMapping("/books/{bookId}/reviews/me/view")
    public String getMyReviewFragment(@PathVariable Long bookId,
                                      @RequestHeader(value = "Cookie", required = false) String cookie,
                                      Model model) {

        // 쿠키가 없으면(비로그인) -> 작성 폼을 보여주기 위해 null 전달
        if (cookie == null || !cookie.contains("CART_ID")) { // "CART_ID" 대신 실제 로그인 쿠키 이름 확인 (예: "Authorization" 등)
            // 혹은 frontReviewService 내부에서 토큰 검증 후 예외가 발생하면 catch해서 처리
        }

        try {
            BookReviewResponse myReview = frontReviewService.getMyReview(bookId, cookie);
            model.addAttribute("myReview", myReview);
        } catch (Exception e) {
            // 비로그인이거나, 리뷰를 안 쓴 경우
            model.addAttribute("myReview", null);
        }

        return "Book/review/my-review"; // 경로 확인!
    }

    // [API] 리뷰 작성 (Multipart/form-data)
    @PostMapping(value = "/api/books/{bookId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> createReview(@PathVariable Long bookId,
                                               @RequestPart("request") ReviewCreateRequest request,
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                               @RequestHeader(value = "Cookie", required = false) String cookie) {

        frontReviewService.createReview(bookId, request, images, cookie);
        return ResponseEntity.status(201).body("리뷰가 등록되었습니다.");
    }

    // [API] 리뷰 수정
    @PutMapping(value = "/api/books/{bookId}/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> updateReview(@PathVariable Long bookId,
                                               @PathVariable Long reviewId,
                                               @RequestPart("request") ReviewUpdateRequest request, // 파라미터 이름 통일 ("request")
                                               @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                               @RequestHeader(value = "Cookie", required = false) String cookie) {

        frontReviewService.updateReview(bookId, reviewId, request, images, cookie);
        return ResponseEntity.ok("리뷰가 수정되었습니다.");
    }

    // [API] 리뷰 삭제
    @DeleteMapping("/api/reviews/{reviewId}")
    @ResponseBody
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId,
                                             @RequestHeader(value = "Cookie", required = false) String cookie) {
        frontReviewService.deleteReview(reviewId, cookie);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}")
    public String viewBookDetail(@PathVariable Long bookId, Model model) {

        // [테스트용 가짜 데이터] 백엔드 연동 전 임시 사용
        BookDetailResponse mockBook = new BookDetailResponse(
                bookId,
                "테스트 책 제목입니다",
                "홍길동",
                "하이파이브 출판사",
                20000,
                "2024-12-08",
                "http://placehold.it/300x400", // 이미지 더미
                "이 책은 테스트용 설명입니다."
        );

        model.addAttribute("book", mockBook);

        return "Book/book-detail"; // html 파일명
    }

}