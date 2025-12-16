package com.example.high_five.controller;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class testController {

    private final BookClient bookClient; // FeignClient 주입

    @GetMapping("/")
    public String mainPage(Model model) {

        try {
            // 1. FeignClient를 통해 백엔드(Gateway -> Book Server) 호출
            List<BookResponse> newBooks = bookClient.getNewBooks();
            // 2. 뷰(Thymeleaf)로 전달
            model.addAttribute("newBooks", newBooks);

            List<BookResponse> risingBooks = bookClient.getPopularBooks();
            System.out.println("메인 컨트롤러 - 주간 인기 도서 개수: " + risingBooks.size());
            model.addAttribute("risingBooks", risingBooks);

            List<BookResponse> bestSellers = bookClient.getBestSellers();
            model.addAttribute("bestSellers", bestSellers); // 모델에 담기

        } catch (Exception e) {
            // 백엔드 서버가 죽어있거나 에러가 나도 메인 페이지는 떠야 함
            e.printStackTrace();
            model.addAttribute("newBooks", List.of()); // 빈 리스트 전달
        }
        return "index";
    }
}