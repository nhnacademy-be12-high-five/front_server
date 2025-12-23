package com.example.high_five.controller.member;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(@ModelAttribute MemberCreateRequestDto requestDto) {
        try {
            authService.signup(requestDto);
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage());
            return "redirect:/member/signup?error=true";
        }
    }

    @LoginRequired
    @GetMapping("/mypage")
    public String myPage(Model model) {
        try {
            var myInfo = memberService.getMyInfo().getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (FeignException e) {
            log.error("내 정보 조회 실패: {}", e.getMessage());
            return "redirect:/member/login";
        }
        model.addAttribute("currentTab", "info");
        return "mypage/myinfo";
    }

    @LoginRequired
    @PostMapping("/mypage/update")
    public String updateMember(@ModelAttribute MemberUpdateRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            memberService.updateMember(request);
            redirectAttributes.addFlashAttribute("alertMessage", "수정되었습니다.");
        } catch (FeignException e) {
            String msg = extractFeignMessage(e); // 아래 함수
            redirectAttributes.addFlashAttribute("alertMessage", msg);
        }
        return "redirect:/mypage";
    }

    @LoginRequired
    @PostMapping("/mypage/withdraw")
    public String withdrawMember(HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            memberService.withdrawMember();
            deleteCookie(response, "access-token");
            deleteCookie(response, "refresh-token");
            redirectAttributes.addFlashAttribute("message", "탈퇴되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "탈퇴 실패");
            return "redirect:/mypage";
        }
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String extractFeignMessage(FeignException e) {
        try {
            String body = e.contentUTF8();
            if (body == null || body.isBlank()) return "요청 처리 중 오류가 발생했습니다.";

            JsonNode node = objectMapper.readTree(body);

            if (node.has("message")) return node.get("message").asText();
            return body;
        } catch (Exception ex) {
            return "요청 처리 중 오류가 발생했습니다.";
        }
    }
}