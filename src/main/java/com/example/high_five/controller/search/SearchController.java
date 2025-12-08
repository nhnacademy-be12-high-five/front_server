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

    /**
     * 일반 검색
     * 예: /search?keyword=유아&sort=POPULAR&page=0
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 20;

        URI uri = UriComponentsBuilder
                .fromHttpUrl(bookApiBaseUrl + "/api/search")
                .queryParam("keyword", keyword)
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
        if (body == null) {
            body = new PagedResponse();
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("books", body.getContent());
        model.addAttribute("pageInfo", body);   // totalElements, totalPages 등
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);

        // 탭/AI 박스 표시용
        model.addAttribute("searchType", "NORMAL");
        model.addAttribute("aiSummary", null);

        return "Book/booklist";
    }

    /**
     * AI 검색 (RAG 기반)
     * 예: /rag-search?keyword=유아&sort=REVIEW&page=0
     */
    @GetMapping("/rag-search")
    public String ragSearch(@RequestParam String keyword,
                            @RequestParam(defaultValue = "POPULAR") String sort,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        int size = 20;

        // 1) 도서 목록 (RAG 하이브리드 검색)
        URI searchUri = UriComponentsBuilder
                .fromHttpUrl(bookApiBaseUrl + "/api/search/rag-search")
                .queryParam("keyword", keyword)
                .queryParam("page", page)
                .queryParam("size", size)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<PagedResponse> response =
                restTemplate.exchange(
                        searchUri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PagedResponse>() {}
                );

        PagedResponse body = response.getBody();
        if (body == null) {
            body = new PagedResponse();
        }

        // 2) AI 요약/추천 문장
        URI answerUri = UriComponentsBuilder
                .fromHttpUrl(bookApiBaseUrl + "/api/search/rag-answer")
                .queryParam("keyword", keyword)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<String> aiResponse =
                restTemplate.getForEntity(answerUri, String.class);

        String aiMessage = aiResponse.getBody();

        // 3) 모델에 담기
        model.addAttribute("keyword", keyword);
        model.addAttribute("books", body.getContent());
        model.addAttribute("pageInfo", body);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);

        // 템플릿에서 사용하는 이름과 맞추기
        model.addAttribute("searchType", "AI");
        model.addAttribute("aiSummary", aiMessage);

        return "Book/booklist";
    }
}
