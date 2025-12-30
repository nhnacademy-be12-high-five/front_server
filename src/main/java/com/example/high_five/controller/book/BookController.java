package com.example.high_five.controller.book;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.context.UserContext;
import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookClient bookClient;
    private final ReviewService reviewService;
    private final CouponService couponService;

    /**
     * 도서 상세 화면
     */
    @GetMapping("/books/{book-id}")
    public String getBookDetail(@PathVariable("book-id") Long id, @RequestParam(value = "page", defaultValue = "0") int page,
                                Model model, @RequestAttribute(value = "user", required = false) UserContext user) {

        Long loginMemberId = null;

        if(user != null) {
            loginMemberId = user.id();
        }

        BookResponse book = bookClient.getBookDetail(id);
        model.addAttribute("book", book);

        try {
            List<Long> categoryIds = new ArrayList<>();
            if (book.categories() != null) {
                categoryIds = book.categories().stream()
                        .map(category -> Long.valueOf(category.categoryId()))
                        .collect(Collectors.toList());
            }
            if (book.categoryId() != null) {
                categoryIds.add(Long.valueOf(book.categoryId()));
            }
            if (book.parentId() != null) {
                categoryIds.add(Long.valueOf(book.parentId()));
            }
            categoryIds = categoryIds.stream().distinct().collect(Collectors.toList());
            log.info("Book ID: {}, Extracted Category IDs: {}", id, categoryIds);
            List<CouponTemplateDto> coupons = couponService.getBookCoupons(id, categoryIds, false);
            model.addAttribute("coupons", coupons);
        } catch (Exception e) {
            // 쿠폰 서비스 장애 시에도 상세 페이지는 나와야 하므로 빈 리스트 처리
            model.addAttribute("coupons", Collections.emptyList());
        }

        try {
            if (loginMemberId != null) {
                BookReviewResponse myReview = reviewService.getMyReview(id);
                model.addAttribute("myReview", myReview);
            } else {
                model.addAttribute("myReview", null);
            }
        } catch (Exception e) {
            model.addAttribute("myReview", null);
        }

        int pageNum = page < 0 ? 0 : page;
        Page<BookReviewResponse> reviewList = reviewService.getReviews(id, PageRequest.of(pageNum, 5));
        model.addAttribute("reviewList", reviewList);

        model.addAttribute("loginMemberId", loginMemberId);

        boolean isLiked = false;
        if (loginMemberId != null) {
            try {
                ResponseEntity<Boolean> likeResponse = bookClient.getLikeStatus(id);
                if (likeResponse != null && likeResponse.getBody() != null) {
                    isLiked = likeResponse.getBody();
                }
            } catch (Exception e) {
                log.warn("좋아요 상태 조회 실패 (BookID: {}, MemberID: {}): {}", id, loginMemberId, e.getMessage());
            }
        }
        model.addAttribute("isLiked", isLiked);
        return "Book/book-detail";
    }
}
