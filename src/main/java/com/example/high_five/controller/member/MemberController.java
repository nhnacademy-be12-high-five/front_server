package com.example.high_five.controller.member;

import com.example.high_five.dto.member.MemberCreateRequestDto;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입 페이지 이동
    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    // 회원가입 처리
    @PostMapping("/member/signup")
    public String signup(@ModelAttribute MemberCreateRequestDto requestDto) {
        try {
            memberService.registerMember(requestDto);
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "redirect:/member/signup?error";
        }
    }
}