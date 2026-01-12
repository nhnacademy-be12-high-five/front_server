package com.example.high_five.controller.cart;

import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController // JSON을 반환하므로 Controller가 아닌 RestController 사용
@RequiredArgsConstructor
public class CartRestController {

    // Book Server와 통신하는 Feign Client (이미 있다고 가정)
    private final BookFeignClient bookFeignClient;

    @PostMapping("/books/recommendations") // JS의 fetch 주소와 일치해야 함
    public ResponseEntity<List<BookResponse>> getAiRecommendations(@RequestBody List<String> titles) {
        // Book Server로 요청 위임
        return bookFeignClient.getAiRecommendations(titles);
    }
}