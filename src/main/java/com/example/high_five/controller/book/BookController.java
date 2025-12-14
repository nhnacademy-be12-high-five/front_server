package com.example.high_five.controller.book;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookClient bookClient;
    private final ReviewService reviewService;
    private final CouponService couponService;

    /**
     * 도서 상세 화면
     */
    @GetMapping("/books/{bookId}")
    public String getBookDetail(@PathVariable("bookId") Long id, Model model) {

        BookResponse book = bookClient.getBookDetail(id);
        model.addAttribute("book", book);

        try {
            List<CouponTemplateDto> coupons = couponService.getBookCoupons(id);
            model.addAttribute("coupons", coupons);
        } catch (Exception e) {
            // 쿠폰 서비스 장애 시에도 상세 페이지는 나와야 하므로 빈 리스트 처리
            model.addAttribute("coupons", Collections.emptyList());
        }

        try {
            // 로그인 된 상태라면 내 리뷰 조회, 아니면 null 처리 등의 로직 필요
            // 여기서는 단순하게 서비스 호출 (없으면 null 반환 가정)
            BookReviewResponse myReview = reviewService.getMyReview(id);
            model.addAttribute("myReview", myReview);
        } catch (Exception e) {
            // 로그인이 안 되어 있거나 리뷰가 없으면 null로 넘김 -> HTML에서 작성 폼 뜸
            model.addAttribute("myReview", null);
        }

        // ---------------------------------------------------------
        // [수정 3] 다른 사람들 리뷰 리스트도 가져와서 담기
        // ---------------------------------------------------------
        Page<BookReviewResponse> reviewList = reviewService.getReviews(id, Pageable.ofSize(5));
        model.addAttribute("reviewList", reviewList);

        Long memberId = 1L;
        boolean isLiked = false;

        if (memberId != null){
            try {
                ResponseEntity<Boolean> likeResponse = bookClient.getLikeStatus(id, memberId);
            }
            catch (Exception e) {
                // 에러 나도 페이지는 떠야 하므로 로그만 찍고 false 유지
                System.err.println("좋아요 상태 조회 실패: " + e.getMessage());
            }
        }

        // 마이페이지- 찜목록으로 이동하도록 구현
         model.addAttribute("isLiked", isLiked);
        return "Book/book-detail";
    }
}
