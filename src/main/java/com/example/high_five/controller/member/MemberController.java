package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(@ModelAttribute MemberCreateRequestDto requestDto) {
        try {
            memberService.registerMember(requestDto);
            return "redirect:/member/login.html";
        } catch (Exception e) {
            log.error(e.getMessage());
            return "redirect:/member/signup?error";
        }
    }

    @GetMapping("/mypage")
    public String myPage(
            @CookieValue(value = "access-token", required = false) String accessToken,
            @RequestParam(value = "tab", required = false, defaultValue = "info") String tab,
            Model model) {

        if (accessToken == null) return "redirect:/member/login.html";

        String authHeader = "Bearer " + accessToken;

        try {
            var myInfo = memberService.getMyInfo(authHeader).getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (Exception e) {
            log.error("내 정보 조회 실패: {}", e.getMessage());
            return "redirect:/member/login.html";
        }

        model.addAttribute("currentTab", tab);
        return "member/mypage";
    }

}