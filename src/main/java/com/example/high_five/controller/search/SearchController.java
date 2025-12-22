package com.example.high_five.controller.search;

import com.example.high_five.dto.book.PagedResponse;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final BookClient bookClient;  // ★ FeignClient 주입

    private static final int PAGE_SIZE = 10;

    /**
     * 일반 검색 + 카테고리 검색 공통 처리
     */
    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(value = "searchType", required = false, defaultValue = "NORMAL") String searchType,
                         @RequestParam(value = "categoryId", required = false) Long categoryId,
                         @RequestParam(value = "categoryName", required = false) String categoryName,
                         @RequestParam(value = "sort", defaultValue = "POPULAR") String sort,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {

        // =====================
        // 1) 카테고리 검색 모드
        // =====================
        // SearchController.java 내부의 카테고리 로직 부분

        if ("CATEGORY".equalsIgnoreCase(searchType) && categoryId != null) {
            // BookClient를 통해 북 서버의 API 호출
            List<BookResponse> books = bookClient.getBooksByCategory(categoryId.intValue());

            model.addAttribute("searchType", "CATEGORY");
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("categoryName", categoryName); // HTML에서 제목으로 사용
            model.addAttribute("books", books); // 검색 결과 리스트

            // 페이징이나 AI 요약은 카테고리 검색에선 사용하지 않으므로 null 처리
            model.addAttribute("pageInfo", null);
            model.addAttribute("aiSummary", null);

            return "Book/booklist";
        }

        // =====================
        // 2) 일반 검색(NORMAL)
        // =====================
        // keyword가 없으면 홈으로 돌려보내기
        if (keyword == null || keyword.isBlank()) {
            return "redirect:/";
        }

        PagedResponse response =
                bookClient.search(keyword, sort, page, PAGE_SIZE);

        if (response == null) {
            response = new PagedResponse();
        }

        int currentPage = response.getNumber();
        response.setPage(currentPage);

        model.addAttribute("searchType", "NORMAL");
        model.addAttribute("keyword", keyword);
        model.addAttribute("books", response.getContent());
        model.addAttribute("pageInfo", response);
        model.addAttribute("page", currentPage);
        model.addAttribute("sort", sort);
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

        int size = PAGE_SIZE;

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
