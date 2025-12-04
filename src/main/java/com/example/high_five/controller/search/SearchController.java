package com.example.high_five.controller.search;

import com.example.high_five.dto.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final RestTemplate restTemplate;

    @Value("${book-server.base-url}")
    private String bookServerBaseUrl;

    // ✅ 이 index() 메서드는 삭제하거나 주석 처리하세요
    // @GetMapping("/")
    // public String index() {
    //     return "index";
    // }

    // 검색 결과 페이지
    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {

        String encoded = UriUtils.encode(keyword, StandardCharsets.UTF_8);
        String url = bookServerBaseUrl + "/api/search?keyword=" + encoded;

        // book_server 응답 DTO 타입에 맞게 수정 (예: BookResponse[])
        ResponseEntity<BookResponse[]> response =
                restTemplate.getForEntity(url, BookResponse[].class);

        model.addAttribute("keyword", keyword);
        model.addAttribute("books", response.getBody());

        // templates/Book/booklist.html
        return "Book/booklist";
    }
}
