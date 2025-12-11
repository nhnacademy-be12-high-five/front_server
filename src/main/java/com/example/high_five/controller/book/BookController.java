package com.example.high_five.controller.book;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookClient bookClient;

    /**
     * 도서 상세 화면
     */

    @GetMapping("/books/{book-id}")
    public String getBookDetail(@PathVariable("book-id") Long id, Model model) {

        BookResponse book = bookClient.getBookDetail(id);

        model.addAttribute("book", book);

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

        return "Book/book-detail";
    }
}