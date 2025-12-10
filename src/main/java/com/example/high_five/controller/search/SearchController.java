package com.example.high_five.controller.search;

import com.example.high_five.dto.book.BookResponse;
import com.example.high_five.dto.book.PagedResponse;
import com.example.high_five.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final BookService bookService; // FeignClient 주입

    /**
     * 일반 검색
     */
    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "POPULAR") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {

        int size = 20;

        // Feign 호출 (PagedResponse<BookResponse>로 반환됨)
        PagedResponse<BookResponse> body = bookService.search(keyword, sort, page, size);

        // Null 처리 (Feign은 보통 예외를 던지거나 null을 줄 수 있음, 필요 시 빈 객체 처리)
        if (body == null) {
            body = new PagedResponse<>();
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

        int size = 20;

        // 1) 도서 목록 (RAG 하이브리드 검색)
        PagedResponse<BookResponse> body = bookService.ragSearch(keyword, page, size);

        if (body == null) {
            body = new PagedResponse<>();
        }

        // 2) AI 요약/추천 문장
        String aiMessage = bookService.ragAnswer(keyword);

        // 3) 모델에 담기
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