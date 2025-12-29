package com.example.high_five.controller.book;

import com.example.high_five.service.BookClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookLikeControllerTest {

    @InjectMocks
    private BookLikeController bookLikeController;

    @Mock
    private BookClient bookClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookLikeController).build();
    }

    @Test
    @DisplayName("좋아요 상태 조회")
    void likeStatus() throws Exception {
        Long bookId = 1L;
        given(bookClient.getLikeStatus(bookId)).willReturn(ResponseEntity.ok(true));

        mockMvc.perform(get("/books/{bookId}/likes/status", bookId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(bookClient).getLikeStatus(bookId);
    }

    @Test
    @DisplayName("좋아요 토글")
    void toggle() throws Exception {
        Long bookId = 1L;
        given(bookClient.toggleLike(bookId)).willReturn(ResponseEntity.ok(false)); // 좋아요 취소됨

        mockMvc.perform(post("/books/{bookId}/likes", bookId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(bookClient).toggleLike(bookId);
    }
}