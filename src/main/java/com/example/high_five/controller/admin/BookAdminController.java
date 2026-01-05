package com.example.high_five.controller.admin;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.book.BookInfoDto;
import com.example.high_five.dto.book.BookPagedResponse;
import com.example.high_five.dto.book.ParsingDto;
import com.example.high_five.dto.book.request.BookAdminUpdateRequest;
import com.example.high_five.dto.book.request.BookRequest;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.BookClient;
import com.example.high_five.service.BookFeignClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/books")
public class BookAdminController {
    private final BookClient bookClient;
    private final BookFeignClient bookFeignClient;

    @GetMapping
    @LoginRequired(adminOnly = true)
    public String bookPage(Model model) {
        return "admin/books";
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<BookResponse>> searchBooks(@RequestParam("keyword") String keyword) {
        try {
            BookPagedResponse<BookResponse> response = bookFeignClient.searchBooks(keyword, 0, 20);
            return ResponseEntity.ok(response != null ? response.getContent() : Collections.emptyList());
        } catch (Exception e) {
            log.error("도서 검색 실패", e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    @LoginRequired(adminOnly = true)
    public ResponseEntity<BookResponse> getBookDetail(@PathVariable Long id) {
        try {
            BookResponse book = bookClient.getBookDetail(id);
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            log.error("도서 상세 조회 실패", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @LoginRequired(adminOnly = true)
    public String createBook(@ModelAttribute BookRequest bookRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            bookClient.createBook(bookRequest);
            log.info("도서 동록 성공 : {}",bookRequest);
            redirectAttributes.addFlashAttribute("message", "도서가 성공적으로 등록되었습니다.");
        } catch (FeignException e) {
            log.error("도서 등록 실패 (Status: {}): {}", e.status(), e.getMessage());
            if (e.status() == 409) { // 409 Conflict (중복)
                redirectAttributes.addFlashAttribute("errorMessage", "이미 등록된 도서(ISBN 중복)입니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "도서 등록에 실패했습니다. 입력 값을 확인해주세요.");
            }
        }
        return "redirect:/admin/books";
    }

    @GetMapping("/search-api")
    @ResponseBody
    @LoginRequired(adminOnly = true)
    public ResponseEntity<BookInfoDto> searchBookWithAi(@RequestParam("isbn") String isbn) {
        // Book Server로 요청 위임
        return bookFeignClient.searchBookByIsbn(isbn);
    }

    @PostMapping("/{id}/update")
    @LoginRequired(adminOnly = true)
    public String updateBook(@PathVariable Long id,
                             @ModelAttribute BookAdminUpdateRequest updateRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            log.info("도서 수정 요청 - ID: {}, 제목: {}", id, updateRequest.getTitle());

            // 백엔드 API 호출
            bookClient.updateBook(id, updateRequest);

            redirectAttributes.addFlashAttribute("message", "도서 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            log.error("도서 수정 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "도서 수정 중 오류가 발생했습니다.");
        }

        // 목록 페이지로 리다이렉트 (새로고침 시 재전송 방지)
        return "redirect:/admin/books";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @LoginRequired(adminOnly = true)
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        try {
            log.info("도서 삭제 요청 - I: {}", id);
            bookClient.deleteBook(id);
            return ResponseEntity.ok("삭제되었습니다.");
        }catch (Exception e) {
            log.error("도서 삭제 실패", e);
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }
}
