package com.example.high_five.controller.search;

import com.example.high_five.dto.book.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final RestTemplate restTemplate;

    @Value("${book.api.base-url}")
    private String bookApiBaseUrl;

    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 20;  // 페이지당 도서 개수


        URI uri = UriComponentsBuilder
                .fromHttpUrl(bookApiBaseUrl + "/api/search")
                .queryParam("keyword", keyword)       // raw 값
                .queryParam("sort", sort)
                .queryParam("page", page)
                .queryParam("size", size)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<PagedResponse> response =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PagedResponse>() {}
                );

        PagedResponse body = response.getBody();

        model.addAttribute("keyword", keyword);
        model.addAttribute("books", body != null ? body.getContent() : null);
        model.addAttribute("totalElements", body != null ? body.getTotalElements() : 0);
        model.addAttribute("totalPages", body != null ? body.getTotalPages() : 0);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);

        return "Book/booklist"; // 검색 결과 템플릿 이름
    }
}
