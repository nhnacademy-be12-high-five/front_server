package com.example.high_five.controller.review;

import com.example.high_five.dto.context.UserContext;
import com.example.high_five.dto.review.BookReviewResponse;
import com.example.high_five.dto.review.ReviewCreateRequest;
import com.example.high_five.dto.review.ReviewUpdateRequest;
import com.example.high_five.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest; // [추가]
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @InjectMocks
    private ReviewController reviewController;

    @Mock
    private ReviewService reviewService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()) // Pageable 처리
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("리뷰 등록 - 성공")
    void addReview() throws Exception {
        // given
        Long bookId = 1L;
        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        // when
        mockMvc.perform(multipart("/books/{book-id}/reviews", bookId)
                        .file(image)
                        .param("title", "Review Title")
                        .param("content", "Review Content")
                        .param("rating", "5"))
                .andExpect(status().isCreated());

        // then
        verify(reviewService).createReview(eq(bookId), any(MultipartFile.class), anyList());
    }

    @Test
    @DisplayName("리뷰 리스트 조회 (AJAX) - PageImpl 직렬화 오류 수정")
    void getBookReviewList() throws Exception {
        Long bookId = 1L;
        // [수정] PageRequest를 명시적으로 전달하여 Unpaged 관련 예외 방지
        given(reviewService.getReviews(eq(bookId), any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 5), 0));

        mockMvc.perform(get("/books/{book-id}/reviews", bookId)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("리뷰 수정 폼 조회")
    void updateReviewForm() throws Exception {
        Long bookId = 1L;
        BookReviewResponse mockReview = new BookReviewResponse(1L, 1L, "loginId", "contentcontent", 5, ZonedDateTime.now(), null, 3, false); // 적절한 생성자/Setter 사용
        given(reviewService.getMyReview(bookId)).willReturn(mockReview);

        mockMvc.perform(get("/books/{book-id}/reviews/update", bookId))
                .andExpect(status().isOk())
                .andExpect(view().name("review/review-update-form"))
                .andExpect(model().attribute("review", mockReview));
    }

    @Test
    @DisplayName("리뷰 수정 처리 - @RequestPart 사용")
    void updateReviewPut() throws Exception {
        // given
        Long bookId = 1L;
        Long reviewId = 10L;

        ReviewUpdateRequest updateDto = new ReviewUpdateRequest("contentcontent", 5, null);
        String dtoJson = objectMapper.writeValueAsString(updateDto);

        MockMultipartFile requestPart = new MockMultipartFile("request", "", "application/json", dtoJson.getBytes(StandardCharsets.UTF_8));
        MockMultipartFile imagePart = new MockMultipartFile("images", "new.jpg", "image/jpeg", "new".getBytes());

        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/books/{book-id}/reviews/{review-id}", bookId, reviewId)
                        .file(requestPart)
                        .file(imagePart))
                .andExpect(status().isOk());

        // then
        verify(reviewService).updateMyReview(eq(bookId), eq(reviewId), any(MultipartFile.class), anyList());
    }

    @Test
    @DisplayName("마이페이지 리뷰 관리 - 로그인 상태")
    void myReviews_Login() throws Exception {
        // given
        UserContext userContext = new UserContext(1L, "USER");
        // [수정] 여기서도 PageRequest 전달
        given(reviewService.getMyReviews(any(Pageable.class)))
                .willReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0));

        // when
        mockMvc.perform(get("/mypage/reviews")
                        .requestAttr("user", userContext)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/reviews"))
                .andExpect(model().attributeExists("reviews"));
    }

    @Test
    @DisplayName("마이페이지 리뷰 관리 - 비로그인 (리다이렉트)")
    void myReviews_NoLogin() throws Exception {
        mockMvc.perform(get("/mypage/reviews")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    @DisplayName("리뷰 좋아요 토글")
    void toggleLike() throws Exception {
        Long bookId = 1L;
        Long reviewId = 100L;
        given(reviewService.toggleReviewLike(bookId, reviewId)).willReturn(true);

        mockMvc.perform(post("/books/{book-id}/reviews/{review-id}/like", bookId, reviewId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}