package com.example.high_five.controller.book;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.context.UserContext;
import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    @Mock
    private BookClient bookClient;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CouponService couponService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    @Test
    @DisplayName("도서 상세 조회 - 비로그인 사용자, 모든 서비스 정상")
    void getBookDetail_Guest_Success() throws Exception {
        // given
        Long bookId = 1L;
        // BookResponse 생성자 (필드 개수에 맞춰 null 등 채움)
        BookResponse mockBook = new BookResponse(
                bookId, "Title", "Author", "ISBN", 10000, "img",
                Collections.emptyList(), Collections.emptyList(), "Content", "Pub",
                "2024-01-01", 4.5, 10L, "Summary", "ReviewSummary", 1, null
        );

        given(bookClient.getBookDetail(bookId)).willReturn(mockBook);
        given(couponService.getBookCoupons(eq(bookId), anyList(), eq(false))).willReturn(Collections.emptyList());
        given(reviewService.getReviews(eq(bookId), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/books/{book-id}", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/book-detail"))
                .andExpect(model().attribute("book", mockBook))
                .andExpect(model().attribute("isLiked", false))
                .andExpect(model().attributeDoesNotExist("loginMemberId")); // 비로그인 확인
    }

    @Test
    @DisplayName("도서 상세 조회 - 로그인 사용자, 좋아요 True, 내 리뷰 존재")
    void getBookDetail_Login_Success() throws Exception {
        // given
        Long bookId = 1L;
        Long userId = 100L;
        UserContext userContext = new UserContext(userId, "USER");

        BookResponse mockBook = new BookResponse(
                bookId, "Title", "Author", "ISBN", 10000, "img",
                Collections.emptyList(), Collections.emptyList(), "Content", "Pub",
                "2024-01-01", 4.5, 10L, "Summary", "ReviewSummary", 1, null
        );
        BookReviewResponse myReview = new BookReviewResponse(1L, userId, "123", "Content", 3, ZonedDateTime.now(), null, 3, false); // 필요한 필드 채워 생성

        given(bookClient.getBookDetail(bookId)).willReturn(mockBook);
        given(couponService.getBookCoupons(eq(bookId), anyList(), eq(false))).willReturn(Collections.emptyList());
        given(reviewService.getMyReview(bookId)).willReturn(myReview);
        given(reviewService.getReviews(eq(bookId), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));
        given(bookClient.getLikeStatus(bookId)).willReturn(ResponseEntity.ok(true));

        // when & then
        mockMvc.perform(get("/books/{book-id}", bookId)
                        .requestAttr("user", userContext)) // 로그인 정보 주입
                .andExpect(status().isOk())
                .andExpect(view().name("Book/book-detail"))
                .andExpect(model().attribute("loginMemberId", userId))
                .andExpect(model().attribute("myReview", myReview))
                .andExpect(model().attribute("isLiked", true));
    }

    @Test
    @DisplayName("도서 상세 조회 - 외부 서비스 예외 발생 시 페이지 정상 로드 (Resilience)")
    void getBookDetail_ServiceFailure() throws Exception {
        // given
        Long bookId = 1L;
        Long userId = 100L;
        UserContext userContext = new UserContext(userId, "USER");

        // 필수인 책 정보는 성공한다고 가정
        BookResponse mockBook = new BookResponse(
                bookId, "Title", "Author", "ISBN", 10000, "img",
                Collections.emptyList(), Collections.emptyList(), "Content", "Pub",
                "2024-01-01", 4.5, 10L, "Summary", "ReviewSummary", 1, null
        );
        given(bookClient.getBookDetail(bookId)).willReturn(mockBook);

        // 예외 상황 시뮬레이션
        given(couponService.getBookCoupons(eq(bookId), anyList(), eq(false))).willThrow(new RuntimeException("Coupon Service Down"));
        given(reviewService.getMyReview(bookId)).willThrow(new RuntimeException("Review Service Down"));
        given(bookClient.getLikeStatus(bookId)).willThrow(new RuntimeException("Like Service Down"));

        given(reviewService.getReviews(eq(bookId), any(PageRequest.class)))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/books/{book-id}", bookId)
                        .requestAttr("user", userContext))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/book-detail"))
                .andExpect(model().attribute("coupons", Collections.emptyList())) // 예외 시 빈 리스트
                .andExpect(model().attribute("myReview", org.hamcrest.Matchers.nullValue())) // 예외 시 null
                .andExpect(model().attribute("isLiked", false)); // 예외 시 false
    }
}