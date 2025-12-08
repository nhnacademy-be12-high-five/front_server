package com.example.high_five.controller.book;

import com.example.high_five.dto.book.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final RestTemplate restTemplate;

    @Value("${book.api.base-url}")
    private String bookApiBaseUrl;

    /**
     * 도서 상세 화면
     * 예: /book/1 -> 북서버 /api/books/1 호출 후 book-detail.html 렌더링
     */
    @GetMapping("/book/{id}")
    public String getBookDetail(@PathVariable("id") Long id, Model model) {

        String url = bookApiBaseUrl + "/api/books/" + id;

        ResponseEntity<BookResponse> response =
                restTemplate.getForEntity(url, BookResponse.class);

        BookResponse book = response.getBody();
        model.addAttribute("book", book);

        // templates/Book/book-detail.html
        return "Book/book-detail";
    }
}
