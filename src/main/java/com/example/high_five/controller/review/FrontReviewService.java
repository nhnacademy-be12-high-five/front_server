package com.example.high_five.controller.review;

import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.dto.review.MyPageReviewResponse;
import com.example.high_five.dto.review.ReviewCreateRequest;
import com.example.high_five.dto.review.ReviewUpdateRequest;
import com.example.high_five.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FrontReviewService {

    private final ReviewService reviewAdaptor;

    // 1. 리뷰 등록
    public void createReview(Long bookId, ReviewCreateRequest request, List<MultipartFile> images) {
        // Feign Client 호출 (헤더 처리는 Interceptor나 Gateway에서 처리된다고 가정하거나, 필요 시 파라미터로 쿠키 전달)
        reviewAdaptor.createReview(bookId, request, images);
    }

    // 2. 내 리뷰 리스트 조회 (마이페이지용)
    public Page<MyPageReviewResponse> getMyReviews(Pageable pageable) {
        return reviewAdaptor.getMyReviews(pageable).getBody();
    }

    // 3. 내 리뷰 단건 조회 (수정 화면 등에서 사용)
    public BookReviewResponse getMyReview(Long bookId) {
        try {
            return reviewAdaptor.getMyReview(bookId).getBody();
        } catch (Exception e) {
            return null; // 리뷰가 없는 경우 처리
        }
    }

    // 4. 특정 책의 리뷰 리스트 조회
    public Page<BookReviewResponse> getBookReviews(Long bookId, Pageable pageable) {
        return reviewAdaptor.getReviews(bookId, pageable).getBody();
    }

    // 5. 리뷰 수정
    public void updateReview(Long bookId, Long reviewId, ReviewUpdateRequest request, List<MultipartFile> images) {
        reviewAdaptor.updateMyReview(bookId, reviewId, request, images);
    }

    // 6. 리뷰 삭제
    public void deleteReview(Long reviewId) {
        reviewAdaptor.removeReview(reviewId);
    }
}
