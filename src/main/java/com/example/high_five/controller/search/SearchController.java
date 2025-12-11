package com.example.high_five.controller.search;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchController {

    private final RestTemplate restTemplate;

    @Value("${book.api.base-url}")
    private String bookApiBaseUrl;

    // === (기존) 일반 검색 메서드는 그대로 두고 ===
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 10;

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
        model.addAttribute("pageInfo", body);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);

        model.addAttribute("searchType", "NORMAL");
        model.addAttribute("aiSummary", null);

        return "Book/booklist";
    }

    /**
     * AI 검색 (RAG 기반)
     */
    @GetMapping("/rag-search")
    public String ragSearch(@RequestParam String keyword,
                            @RequestParam(defaultValue = "POPULAR") String sort,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        int size = 10;

        // 1) 도서 목록 (RAG 하이브리드 검색)
        PagedResponse body;
        try {
            URI searchUri = UriComponentsBuilder
                    .fromHttpUrl(bookApiBaseUrl + "/api/search/rag-search")
                    .queryParam("keyword", keyword)
                    .queryParam("sort", sort)
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

            body = response.getBody();
            if (body == null) {
                body = new PagedResponse();
            }
        } catch (Exception e) {
            // 북서버가 500을 주더라도 여기서 한 번 막고, 화면은 살려 둡니다.
            log.error("RAG 검색 API 호출 실패", e);
            body = new PagedResponse();   // content 비어있는 상태
        }

        // 2) AI 요약/추천 문장 (에러 나도 화면은 유지)
        String aiMessage;
        try {
            URI answerUri = UriComponentsBuilder
                    .fromHttpUrl(bookApiBaseUrl + "/api/search/rag-answer")
                    .queryParam("keyword", keyword)
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            ResponseEntity<String> aiResponse =
                    restTemplate.getForEntity(answerUri, String.class);

            aiMessage = aiResponse.getBody();
        } catch (Exception e) {
            aiMessage = "현재 AI 추천 설명을 불러오지 못했습니다. 나중에 다시 시도해 주세요.";
        }

        // 3) 모델 세팅
        model.addAttribute("keyword", keyword);
        model.addAttribute("books", body.getContent());
        model.addAttribute("pageInfo", body);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);

        model.addAttribute("searchType", "AI");
        model.addAttribute("aiSummary", aiMessage);

        return "Book/booklist";
    }
}
