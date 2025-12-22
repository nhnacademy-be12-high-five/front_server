package com.example.high_five.controller.Tag;

import com.example.high_five.dto.Tag.request.TagRequest;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/tags") // ★ 핵심 수정: 주소를 메인('/')과 겹치지 않게 분리했습니다.
public class TagController {

    private final TagService tagService;

    /**
     * 태그 목록 페이지 조회
     * 실제 URL: GET /admin/tags
     */
    @GetMapping
    public String viewTagPage(Model model) {
        // 1. 백엔드에서 태그 목록 가져오기
        List<BookResponse> tags = tagService.getTags();

        // 2. 모델에 담아서 HTML로 전달
        model.addAttribute("tags", tags);

        // 3. 뷰(HTML) 이름 반환 (resources/templates/admin/tag-list.html)
        return "admin/tag-list";
    }

    /**
     * 태그 등록 처리
     * 실제 URL: POST /admin/tags
     */
    @PostMapping
    public String createTag(@ModelAttribute TagRequest tagRequest) {
        // 1. 백엔드에 생성 요청
        tagService.createTag(tagRequest);

        // 2. 등록이 끝나면 목록 페이지로 새로고침 (Redirect)
        return "redirect:/admin/tags";
    }
}