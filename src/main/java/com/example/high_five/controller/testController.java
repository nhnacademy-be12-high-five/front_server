package com.example.high_five.controller;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class testController {

    private final BookClient bookClient; // FeignClient 주입
    private final TagService tagService;

    @GetMapping("/")
    public String mainPage(Model model) {

        try {
            // 1. FeignClient를 통해 백엔드(Gateway -> Book Server) 호출
            List<BookResponse> newBooks = bookClient.getNewBooks(5);
            // 2. 뷰(Thymeleaf)로 전달
            model.addAttribute("newBooks", newBooks);

            List<BookResponse> risingBooks = bookClient.getPopularBooks(5);
            model.addAttribute("risingBooks", risingBooks);

            List<BookResponse> bestSellers = bookClient.getBestSellers(5);
            model.addAttribute("bestSellers", bestSellers); // 모델에 담기

        } catch (Exception e) {
            // 백엔드 서버가 죽어있거나 에러가 나도 메인 페이지는 떠야 함
            e.printStackTrace();
            model.addAttribute("newBooks", List.of());
            model.addAttribute("risingBooks", List.of());
            model.addAttribute("bestSellers", List.of());
        }
        return "index";
    }

//    @GetMapping("/books/{bookId}")
//    public String getBookDetail(@PathVariable("bookId") Long bookId, Model model) {
//        BookResponse book = bookClient.getBookDetail(bookId);
//        model.addAttribute("book", book);
//
////        List<TagResponse> tags = tagService.getTags();
//        return "Book/book-detail";
//    }

    // 이렇게 하면 이제 "best-seller"는 숫자가 아니므로 위 메서드를 무시하고
    // 아래 메서드를 정확하게 찾아갑니다.

    @GetMapping("/books/best-seller")
    public String bestSellerPage(Model model) {
        List<BookResponse> bestSellers = bookClient.getBestSellers(10);
        model.addAttribute("bestSellers", bestSellers);
        return "Book/bestseller";
    }

    @GetMapping("/books/popular")
    public String getPopularBooks(Model model){
        List<BookResponse> popular=bookClient.getPopularBooks(10);
        model.addAttribute("risingBooks",popular);
        return "Book/weeklyPopular";
    }

    @GetMapping("/books/new")
    public String getNewBooks(Model model){
        List<BookResponse> newBooks=bookClient.getNewBooks(10);
        model.addAttribute("newBooks",newBooks);
        return "Book/new";
    }
}