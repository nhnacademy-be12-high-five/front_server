package com.example.high_five.controller.member;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.service.MemberService;
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

    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    @LoginRequired
    @GetMapping("/mypage")
    public String myPage(Model model) {
        try {
            var myInfo = memberService.getMyInfo().getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (FeignException e) {
            log.error("내 정보 조회 실패 (Feign): {}", e.getMessage());
            return "redirect:/member/login.html";
        } catch (Exception e) {
            log.error("시스템 오류: {}", e.getMessage());
            return "redirect:/";
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
            redirectAttributes.addFlashAttribute("message", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (FeignException e) {
            String errorMessage = "정보 수정 실패";
            if (e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                errorMessage = extractErrorMessage(e.contentUTF8());
            }
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
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
            redirectAttributes.addFlashAttribute("message", "탈퇴가 완료되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            log.error("탈퇴 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "탈퇴 처리에 실패했습니다.");
            return "redirect:/mypage";
        }
    }

    // 쿠키 삭제 헬퍼
    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private String extractErrorMessage(String json) {
        try {
            if (json.contains("\"message\":\"")) {
                int start = json.indexOf("\"message\":\"") + 11;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
        } catch (Exception e) {
            return json;
        }
        return json;
    }
}