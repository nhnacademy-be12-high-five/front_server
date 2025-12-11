package com.example.high_five.controller.search;

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

    /**
     * 일반 검색 + 카테고리 검색 (searchType 으로 구분)
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) String categoryName,
                         @RequestParam(defaultValue = "NORMAL") String searchType,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 10;
        PagedResponse body = new PagedResponse();

        // ===== 1) CATEGORY 모드 =====
        if ("CATEGORY".equalsIgnoreCase(searchType) && categoryId != null) {
            try {
                //  북서버 카테고리별 도서 조회 API 사용
                URI uri = UriComponentsBuilder
                        .fromHttpUrl(bookApiBaseUrl + "/api/categories/" + categoryId + "/books")
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

                if (response.getBody() != null) {
                    body = response.getBody();
                }
            } catch (Exception e) {
                // 에러 나더라도 화면은 살리고 로그만 찍기
                log.error("카테고리 검색 API 호출 실패 categoryId={}, sort={}, page={}",
                        categoryId, sort, page, e);
            }

            model.addAttribute("keyword", null);  // 카테고리 모드라 키워드는 없음
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("books", body.getContent());
            model.addAttribute("pageInfo", body);
            model.addAttribute("page", page);
            model.addAttribute("sort", sort);
            model.addAttribute("searchType", "CATEGORY");
            model.addAttribute("aiSummary", null);

            return "Book/booklist";
        }

        // ===== 2) NORMAL 모드 (기존 검색) =====
        if (keyword == null || keyword.isBlank()) {
            return "redirect:/";
        }

        try {
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

            if (response.getBody() != null) {
                body = response.getBody();
            }
        } catch (Exception e) {
            log.error("일반 검색 API 호출 실패 keyword={}, sort={}, page={}",
                    keyword, sort, page, e);
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", null);
        model.addAttribute("categoryName", null);
        model.addAttribute("books", body.getContent());
        model.addAttribute("pageInfo", body);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);
        model.addAttribute("searchType", "NORMAL");
        model.addAttribute("aiSummary", null);

        return "Book/booklist";
    }


    /**
     * AI 검색 (RAG 기반) – 기존 그대로
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
            log.error("RAG 검색 API 호출 실패", e);
            body = new PagedResponse();
        }

        // 2) AI 요약/추천 문장
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

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", null);
        model.addAttribute("categoryName", null);
        model.addAttribute("books", body.getContent());
        model.addAttribute("pageInfo", body);
        model.addAttribute("page", page);
        model.addAttribute("sort", sort);
        model.addAttribute("searchType", "AI");
        model.addAttribute("aiSummary", aiMessage);

        return "Book/booklist";
    }
}
