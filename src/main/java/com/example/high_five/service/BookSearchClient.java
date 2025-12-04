package com.example.high_five.service;

import com.example.high_five.dto.BookResponse;
import com.example.high_five.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class BookSearchClient {

    private final RestTemplate restTemplate;

    @Value("${book.api.base-url}")
    private String baseUrl;

    /**
     * @param keyword  검색어 (null/빈문자 허용)
     * @param category 카테고리 코드 (domestic, foreign, it, economy, self, investment, all 등)
     * @param sort     정렬 기준 (POPULAR, NEW, PRICE_ASC, PRICE_DESC, RATING, REVIEW)
     */
    public PagedResponse<BookResponse> searchBooks(
            String keyword,
            String category,
            String sort,
            int page,
            int size
    ) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/search");

        // 검색어가 있을 때만 파라미터 추가
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }

        // 카테고리: all 이나 null이면 서버에 안 넘김(전체 검색)
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            builder.queryParam("category", category);
        }

        // 정렬 기본값: POPULAR
        String sortValue = (sort == null || sort.isBlank()) ? "POPULAR" : sort;
        builder.queryParam("sort", sortValue);

        builder.queryParam("page", page);
        builder.queryParam("size", size);

        String url = builder.toUriString();

        ResponseEntity<PagedResponse<BookResponse>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PagedResponse<BookResponse>>() {}
                );

        return response.getBody();
    }
}
