package com.example.high_five.controller.admin;

import com.example.high_five.dto.book.BookInfoDto;
import com.example.high_five.dto.book.BookPagedResponse;
import com.example.high_five.dto.book.request.BookAdminUpdateRequest;
import com.example.high_five.dto.book.request.BookRequest;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.BookFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookAdminControllerTest {

    @InjectMocks
    private BookAdminController bookAdminController;

    @Mock
    private BookClient bookClient;

    @Mock
    private BookFeignClient bookFeignClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookAdminController).build();
    }

    @Test
    @DisplayName("도서 관리 페이지 조회")
    void bookPage() throws Exception {
        mockMvc.perform(get("/admin/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/books"));
    }

    @Test
    @DisplayName("도서 검색 - 성공")
    void searchBooks_Success() throws Exception {
        // BookPagedResponse 객체 생성 및 Setter 사용
        BookPagedResponse<BookResponse> response = new BookPagedResponse<>();
        response.setContent(Collections.emptyList());
        response.setTotalPages(1);
        response.setTotalElements(0);

        given(bookFeignClient.searchBooks(anyString(), anyInt(), anyInt())).willReturn(response);

        mockMvc.perform(get("/admin/books/search").param("keyword", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("도서 검색 - 예외 발생 시 빈 리스트 반환")
    void searchBooks_Exception() throws Exception {
        given(bookFeignClient.searchBooks(anyString(), anyInt(), anyInt())).willThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/admin/books/search").param("keyword", "error"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("도서 상세 조회 - 성공")
    void getBookDetail_Success() throws Exception {
        // BookResponse Record 생성자 인자 순서 맞춤 (17개)
        BookResponse book = new BookResponse(
                1L, "Title", "Author", "ISBN", 10000, "imageUrl",
                Collections.emptyList(), Collections.emptyList(), "Content", "Publisher",
                "2024-01-01", 4.5, 10L, "AiSummary", "AiReview", 1, null
        );

        given(bookClient.getBookDetail(1L)).willReturn(book);

        mockMvc.perform(get("/admin/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    @DisplayName("도서 상세 조회 - 실패 (404 Not Found)")
    void getBookDetail_Fail() throws Exception {
        given(bookClient.getBookDetail(anyLong())).willThrow(new RuntimeException("Not Found"));

        mockMvc.perform(get("/admin/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("도서 등록 - 성공")
    void createBook_Success() throws Exception {
        mockMvc.perform(post("/admin/books")
                        .flashAttr("bookRequest", new BookRequest()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attributeExists("message"));

        verify(bookClient).createBook(any(BookRequest.class));
    }

    @Test
    @DisplayName("AI 도서 검색 (ISBN)")
    void searchBookWithAi() throws Exception {
        given(bookFeignClient.searchBookByIsbn("12345")).willReturn(null);

        mockMvc.perform(get("/admin/books/search-api").param("isbn", "12345"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 수정 - 성공")
    void updateBook_Success() throws Exception {
        BookAdminUpdateRequest request = new BookAdminUpdateRequest();
        request.setTitle("Updated Title");

        mockMvc.perform(post("/admin/books/1/update")
                        .flashAttr("bookAdminUpdateRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/books"))
                .andExpect(flash().attribute("message", "도서 정보가 성공적으로 수정되었습니다."));

        verify(bookClient).updateBook(eq(1L), any(BookAdminUpdateRequest.class));
    }
}