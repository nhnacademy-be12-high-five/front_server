//package com.example.high_five.controller.review; // 패키지 확인
//
//import com.example.high_five.dto.review.*;
//import com.example.high_five.service.ReviewService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class FrontReviewService {
//
//    private final ReviewService reviewAdaptor;
//
//    // 1. 리뷰 등록
//    public void createReview(Long bookId, ReviewCreateRequest request, List<MultipartFile> images) {
//        reviewAdaptor.createReview(bookId, request, images);
//    }
//
//    // 2. 내 리뷰 리스트 조회
//    public Page<MyPageReviewResponse> getMyReviews(Pageable pageable, String cookie) {
//        return reviewAdaptor.getMyReviews(pageable, cookie).getBody();
//    }
//
//    // 3. 내 리뷰 단건 조회
//    public BookReviewResponse getMyReview(Long bookId, String cookie) {
//        try {
//            return reviewAdaptor.getMyReview(bookId, cookie).getBody();
//        } catch (Exception e) {
//            // 404 Not Found 등이 뜨면 리뷰 없는 것으로 처리
//            return null;
//        }
//    }
//
//    // 4. 특정 책의 리뷰 리스트 조회
//    public Page<BookReviewResponse> getBookReviews(Long bookId, Pageable pageable) {
//        // 리스트 조회는 비회원도 가능하므로 쿠키 없이 호출해도 됨 (혹은 필요시 추가)
//        return reviewAdaptor.getReviews(bookId, pageable, null).getBody();
//    }
//
//    // 5. 리뷰 수정
//    public void updateReview(Long bookId, Long reviewId, ReviewUpdateRequest request, List<MultipartFile> images, String cookie) {
//        reviewAdaptor.updateMyReview(bookId, reviewId, request, images, cookie);
//    }
//
//    // 6. 리뷰 삭제
//    public void deleteReview(Long reviewId, String cookie) {
//        reviewAdaptor.removeReview(reviewId, cookie);
//    }
//}