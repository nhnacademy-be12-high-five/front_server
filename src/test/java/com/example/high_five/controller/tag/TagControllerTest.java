package com.example.high_five.controller.tag;

import com.example.high_five.controller.Tag.TagController;
import com.example.high_five.dto.Tag.request.TagRequest;
import com.example.high_five.dto.book.response.BookResponse;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @InjectMocks
    private TagController tagController;

    @Mock
    private TagService tagService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tagController).build();
    }

    @Test
    @DisplayName("태그 목록 페이지 조회")
    void viewTagPage() throws Exception {
        // given
        // Controller 코드에 따르면 tagService.getTags()가 List<BookResponse>를 반환함
        List<BookResponse> tags = Collections.emptyList();
        given(tagService.getTags()).willReturn(tags);

        // when & then
        mockMvc.perform(get("/admin/tags"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/tag-list"))
                .andExpect(model().attribute("tags", tags));
    }

    @Test
    @DisplayName("태그 등록 처리")
    void createTag() throws Exception {
        // given
        TagRequest request = new TagRequest(); // DTO 생성 (필요 시 필드 설정)

        // when
        mockMvc.perform(post("/admin/tags")
                        .flashAttr("tagRequest", request)) // @ModelAttribute 바인딩
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/tags"));

        // then
        verify(tagService).createTag(any(TagRequest.class));
    }
}