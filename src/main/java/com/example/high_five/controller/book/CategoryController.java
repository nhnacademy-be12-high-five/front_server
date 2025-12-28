package com.example.high_five.controller.book;

import com.example.high_five.dto.book.CategoryResponse;
import com.example.high_five.service.CategoryFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryFeignClient categoryFeignClient;

    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        List<CategoryResponse> categories = categoryFeignClient.getParentCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable Integer parentId) {
        List<CategoryResponse> categories = categoryFeignClient.getChildCategories(parentId);
        return ResponseEntity.ok(categories);
    }
}
