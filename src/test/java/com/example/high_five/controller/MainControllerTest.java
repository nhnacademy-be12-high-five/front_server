package com.example.high_five.controller;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @InjectMocks
    private MainController mainController;

    @Mock
    private BookClient bookClient;

    @Mock
    private TagService tagService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mainController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    // 테스트용 BookResponse 생성 헬퍼 메서드
    private BookResponse createBookResponse() {
        return new BookResponse(
                1L, "Title", "Author", "ISBN", 10000, "image.jpg",
                Collections.emptyList(), Collections.emptyList(), "Desc", "Publisher",
                LocalDate.now().toString(), 4.5, 10L, "Summary", "ReviewSummary", 1, null
        );
    }

    @Test
    @DisplayName("메인 페이지 조회 - 모든 서비스 정상 호출")
    void mainPage_Success() throws Exception {
        // given
        List<BookResponse> mockBooks = List.of(createBookResponse());

        given(bookClient.getNewBooks(5)).willReturn(mockBooks);
        given(bookClient.getPopularBooks(5)).willReturn(mockBooks);
        given(bookClient.getBestSellers(5)).willReturn(mockBooks);

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("newBooks"))
                .andExpect(model().attributeExists("risingBooks"))
                .andExpect(model().attributeExists("bestSellers"))
                // 데이터가 잘 들어갔는지 확인 (리스트 크기 등)
                .andExpect(model().attribute("newBooks", mockBooks));
    }

    @Test
    @DisplayName("메인 페이지 조회 - 외부 서비스 예외 발생 시 빈 리스트 처리 (Resilience)")
    void mainPage_Exception() throws Exception {
        // given
        // bookClient 호출 시 예외 발생 시뮬레이션
        given(bookClient.getNewBooks(5)).willThrow(new RuntimeException("Book Service Down"));

        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk()) // 예외가 잡혀서 200 OK 반환
                .andExpect(view().name("index"))
                .andExpect(model().attribute("newBooks", Collections.emptyList()))
                .andExpect(model().attribute("risingBooks", Collections.emptyList()))
                .andExpect(model().attribute("bestSellers", Collections.emptyList()));
    }

    @Test
    @DisplayName("베스트셀러 페이지 조회")
    void bestSellerPage() throws Exception {
        // given
        List<BookResponse> books = List.of(createBookResponse());
        given(bookClient.getBestSellers(10)).willReturn(books);

        // when & then
        mockMvc.perform(get("/books/best-seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/bestseller"))
                .andExpect(model().attribute("bestSellers", books));
    }

    @Test
    @DisplayName("인기 도서 페이지 조회")
    void getPopularBooks() throws Exception {
        // given
        List<BookResponse> books = List.of(createBookResponse());
        given(bookClient.getPopularBooks(10)).willReturn(books);

        // when & then
        mockMvc.perform(get("/books/popular"))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/weekly-popular"))
                .andExpect(model().attribute("risingBooks", books));
    }

    @Test
    @DisplayName("신간 도서 페이지 조회")
    void getNewBooks() throws Exception {
        // given
        List<BookResponse> books = List.of(createBookResponse());
        given(bookClient.getNewBooks(10)).willReturn(books);

        // when & then
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/book-new"))
                .andExpect(model().attribute("newBooks", books));
    }
}