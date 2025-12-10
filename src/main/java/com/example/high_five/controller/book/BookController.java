package com.example.high_five.controller.book;

import com.example.high_five.dto.book.BookResponse;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookClient bookClient;

    /**
     * 도서 상세 화면
     */
    @GetMapping("/book/{book-id}")
    public String getBookDetail(@PathVariable("book-id") Long id, Model model) {

        BookResponse book = bookClient.getBookDetail(id);

        model.addAttribute("book", book);

        return "Book/book-detail";
    }
}