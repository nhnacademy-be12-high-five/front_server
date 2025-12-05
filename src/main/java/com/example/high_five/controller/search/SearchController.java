package com.example.high_five.controller.search;

import com.example.high_five.dto.book.BookResponse;
import com.example.high_five.dto.PagedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
public class SearchController {

    private final RestTemplate restTemplate = new RestTemplate();

    // ★ 일단 디버그를 위해 북서버 주소를 하드코딩
    private static final String BOOK_SERVER_BASE_URL = "http://localhost:9003";

    @GetMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "POPULAR") String sort,
            Model model
    ) {

        // 한글/공백 포함 쿼리 파라미터를 UriComponentsBuilder가 알아서 인코딩 하도록 함
        String url = UriComponentsBuilder
                .fromHttpUrl(BOOK_SERVER_BASE_URL)
                .path("/api/search")
                .queryParam("keyword", keyword)   // "만화 스펀지" 그대로 넣기
                .queryParam("sort", sort)
                .queryParam("page", page)
                .queryParam("size", size)
                .build()                          // 아직 인코딩되지 않은 상태
                .encode(StandardCharsets.UTF_8)   // 여기서 UTF-8 기준으로 인코딩
                .toUriString();

        log.info("프론트 → 북서버 검색 요청 URL = {}", url);

        List<BookResponse> books = Collections.emptyList();
        PagedResponse<BookResponse> pageResponse = null;

        try {
            ResponseEntity<PagedResponse<BookResponse>> responseEntity =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<PagedResponse<BookResponse>>() {}
                    );

            pageResponse = responseEntity.getBody();

            if (pageResponse != null && pageResponse.getContent() != null) {
                books = pageResponse.getContent();

                // 디버그용: 앞 5권만 제목 찍기
                books.stream()
                        .limit(5)
                        .forEach(b ->
                                log.info("프론트가 받은 도서: id={}, title={}", b.getId(), b.getTitle()));
            }

            log.info("북서버에서 받은 도서 개수 = {}", books.size());

        } catch (RestClientException e) {
            log.error("북서버 검색 호출 중 오류 발생", e);
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("books", books);
        model.addAttribute("pageInfo", pageResponse);

        return "Book/booklist";
    }
}
