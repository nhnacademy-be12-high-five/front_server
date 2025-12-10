package com.example.high_five.service;

import com.example.high_five.dto.book.CategoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "gateway-server", contextId = "categoryClient", url = "${gateway.uri}")
public interface CategoryFeignClient {
    // 1차 카테고리 조회
    @GetMapping("/api/categories/parent")
    List<CategoryResponse> getParentCategories();

    // 2차 카테고리 조회
    @GetMapping("/api/categories/{parentId}/child")
    List<CategoryResponse> getChildCategories(@PathVariable("parentId") int parentId);
}
