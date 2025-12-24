package com.example.high_five.controller.mypage;

import com.example.high_five.service.BookClient;
import com.example.high_five.dto.book.response.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageLikesController {

    private final BookClient bookClient;

    @GetMapping("/likes")
    public String likes(Model model) {

        // 북서버에서 찜 목록 조회
        List<BookResponse> likedBooks = bookClient.getMyLikedBooks();

        // likes.html에서 쓰는 이름
        model.addAttribute("likedBooks", likedBooks);

        return "mypage/likes";
    }
}
