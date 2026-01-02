package com.example.high_five.controller.book;

import com.example.high_five.dto.book.CategoryResponse;
import com.example.high_five.service.CategoryFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryFeignClient categoryFeignClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    @DisplayName("최상위 카테고리 조회")
    void getRootCategories() throws Exception {
        CategoryResponse cat1 = new CategoryResponse(1, "Domestic");
        given(categoryFeignClient.getParentCategories()).willReturn(List.of(cat1));

        mockMvc.perform(get("/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Domestic"));
    }

    @Test
    @DisplayName("하위 카테고리 조회")
    void getChildCategories() throws Exception {
        int parentId = 1;
        CategoryResponse cat2 = new CategoryResponse(2, "Novel");
        given(categoryFeignClient.getChildCategories(parentId)).willReturn(List.of(cat2));

        mockMvc.perform(get("/categories/{parentId}/children", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Novel"));
    }
}