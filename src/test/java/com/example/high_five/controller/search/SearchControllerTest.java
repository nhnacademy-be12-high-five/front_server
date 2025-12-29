package com.example.high_five.controller.search;

import com.example.high_five.dto.book.PagedResponse;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @InjectMocks
    private SearchController searchController;

    @Mock
    private BookClient bookClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
    }

    @Test
    @DisplayName("카테고리 검색 - 성공 (PagedResponse 반환 수정)")
    void search_Category() throws Exception {
        // given
        Long categoryId = 1L;

        // [수정] List가 아닌 PagedResponse 객체 생성
        PagedResponse<BookResponse> pagedResponse = new PagedResponse<>();
        BookResponse book = createBookResponse(1L);
        pagedResponse.setContent(List.of(book));
        pagedResponse.setNumber(0);
        pagedResponse.setTotalPages(1);

        // [수정] 메서드 시그니처 변경 반영 (id, page, size) 및 리턴 타입 일치
        given(bookClient.getBooksByCategory(eq(categoryId.intValue()), anyInt(), anyInt()))
                .willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/search")
                        .param("searchType", "CATEGORY")
                        .param("categoryId", String.valueOf(categoryId))
                        .param("categoryName", "Novel")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/booklist"))
                .andExpect(model().attribute("searchType", "CATEGORY"))
                .andExpect(model().attribute("categoryId", categoryId))
                .andExpect(model().attribute("categoryName", "Novel"))
                // books는 pagedResponse.getContent()로 꺼내져서 담김
                .andExpect(model().attributeExists("books"))
                // 컨트롤러 로직에 따라 추가된 모델 속성 검증
                .andExpect(model().attributeExists("pageInfo"))
                .andExpect(model().attribute("page", 0));
    }

    @Test
    @DisplayName("일반 검색 - 키워드 없음 (홈 리다이렉트)")
    void search_Normal_NoKeyword() throws Exception {
        mockMvc.perform(get("/search")
                        .param("searchType", "NORMAL")
                        .param("keyword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("일반 검색 - 성공")
    void search_Normal_Success() throws Exception {
        // given
        String keyword = "Java";
        PagedResponse<BookResponse> pagedResponse = new PagedResponse<>();
        pagedResponse.setContent(List.of(createBookResponse(1L)));
        pagedResponse.setNumber(0);

        given(bookClient.search(anyString(), anyString(), anyInt(), anyInt())).willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/search")
                        .param("searchType", "NORMAL")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("sort", "POPULAR"))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/booklist"))
                .andExpect(model().attribute("searchType", "NORMAL"))
                .andExpect(model().attribute("keyword", keyword))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attributeExists("pageInfo"));
    }

    @Test
    @DisplayName("AI 검색 (RAG) - 성공")
    void ragSearch_Success() throws Exception {
        // given
        String keyword = "AI Recommendation";
        List<BookResponse> books = List.of(createBookResponse(1L));
        String aiMessage = "This is AI summary.";

        given(bookClient.ragSearch(keyword)).willReturn(books);
        given(bookClient.ragAnswer(keyword)).willReturn(aiMessage);

        // when & then
        mockMvc.perform(get("/rag-search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("Book/booklist"))
                .andExpect(model().attribute("searchType", "AI"))
                .andExpect(model().attribute("keyword", keyword))
                .andExpect(model().attribute("aiSummary", aiMessage))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    @DisplayName("AI 검색 (RAG) - 답변 생성 실패 시 기본 메시지")
    void ragSearch_AnswerFail() throws Exception {
        // given
        String keyword = "Error Case";
        given(bookClient.ragSearch(keyword)).willReturn(Collections.emptyList());
        given(bookClient.ragAnswer(keyword)).willThrow(new RuntimeException("AI Error"));

        // when & then
        mockMvc.perform(get("/rag-search")
                        .param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(model().attribute("aiSummary", "현재 AI 추천 설명을 불러오지 못했습니다."));
    }

    private BookResponse createBookResponse(Long id) {
        return new BookResponse(id, "Title", "Author", "ISBN", 10000, "url",
                Collections.emptyList(), Collections.emptyList(), "Desc", "Pub",
                "2024-01-01", 4.5, 10L, "AiSummary", "ReviewSummary", 1, null);
    }
}