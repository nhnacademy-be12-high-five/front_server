package com.example.high_five.controller;

import com.example.high_five.dto.BookResponse;
import com.example.high_five.dto.PagedResponse;
import com.example.high_five.service.BookSearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BookViewController {

    private final BookSearchClient bookSearchClient;

    @GetMapping("/books")
    public String showBookList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        PagedResponse<BookResponse> result =
                bookSearchClient.searchBooks(keyword, category, sort, page, size);

        if (result == null) {
            model.addAttribute("books", java.util.Collections.emptyList());
            model.addAttribute("pageInfo", null);
        } else {
            model.addAttribute("books", result.getContent());
            model.addAttribute("pageInfo", result);
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("sort", sort);

        return "Book/booklist";
    }

}
