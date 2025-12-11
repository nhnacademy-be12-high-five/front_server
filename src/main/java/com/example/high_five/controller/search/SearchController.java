package com.example.high_five.controller.search;

import com.example.high_five.dto.book.PagedResponse;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final BookClient bookClient;  // ★ FeignClient 주입

    /**
     * 일반 검색
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 10;

        // ★ Feign 호출로 대체
        PagedResponse response = bookClient.search(keyword, sort, page, size);

        if (response == null) {
            response = new PagedResponse();
        }

        int currentPage = response.getNumber();
        response.setPage(currentPage);

        // model 설정
        model.addAttribute("keyword", keyword);
        model.addAttribute("books", response.getContent());
        model.addAttribute("pageInfo", response);
        model.addAttribute("page", currentPage);
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

        // 1) AI 기반 책 목록 검색
        PagedResponse response = bookClient.ragSearch(keyword, page, size);
        if (response == null) {
            response = new PagedResponse();
        }

        int currentPage = response.getNumber();
        response.setPage(currentPage);

        // 2) AI 요약 문장 가져오기
        String aiMessage;
        try {
            aiMessage = bookClient.ragAnswer(keyword);
        } catch (Exception e) {
            aiMessage = "현재 AI 추천 설명을 불러오지 못했습니다.";
        }

        // model 설정
        model.addAttribute("keyword", keyword);
        model.addAttribute("books", response.getContent());
        model.addAttribute("pageInfo", response);
        model.addAttribute("page", currentPage);
        model.addAttribute("sort", sort);

        model.addAttribute("searchType", "AI");
        model.addAttribute("aiSummary", aiMessage);

        return "Book/booklist";
    }
}
