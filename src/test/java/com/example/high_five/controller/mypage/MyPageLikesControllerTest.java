package com.example.high_five.controller.mypage;

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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MyPageLikesControllerTest {

    @InjectMocks
    private MyPageLikesController myPageLikesController;

    @Mock
    private BookClient bookClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myPageLikesController).build();
    }

    @Test
    @DisplayName("마이페이지 찜 목록 조회")
    void likes() throws Exception {
        // given
        BookResponse book = new BookResponse(
                1L, "Title", "Author", "ISBN", 1000, "img",
                Collections.emptyList(), Collections.emptyList(), "Desc", "Pub",
                "2024-01-01", 4.5, 10L, "Summary", "ReviewSummary", 1, null
        );
        given(bookClient.getMyLikedBooks()).willReturn(List.of(book));

        // when & then
        mockMvc.perform(get("/mypage/likes"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/likes"))
                .andExpect(model().attributeExists("likedBooks"));
    }
}