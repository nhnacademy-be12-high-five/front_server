package com.example.high_five.controller.member;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.request.MemberCreateRequest;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.exception.FeignErrorParser;
import com.example.high_five.service.AuthService;
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
    private final AuthService authService;
    private final FeignErrorParser feignErrorParser;

    @GetMapping("/member/signup")
    public String signupForm() {
        return "member/signup";
    }

    @PostMapping("/member/signup")
    public String signup(@ModelAttribute MemberCreateRequest requestDto) {
        try {
            authService.signup(requestDto);
            return "redirect:/member/login";
        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            return "redirect:/member/signup?error=true";
        }
    }

    @LoginRequired
    @GetMapping("/mypage")
    public String myPage(Model model,
                         @RequestParam(value = "alertCode", required = false) String alertCode) {
        try {
            var myInfo = memberService.getMyInfo().getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (FeignException e) {
            log.error("내 정보 조회 실패: {}", e.getMessage(), e);
            return "redirect:/member/login";
        }

        model.addAttribute("alertCode", alertCode);
        model.addAttribute("currentTab", "info");
        return "mypage/myinfo";
    }

    @LoginRequired
    @PostMapping("/mypage/update")
    public String updateMember(@ModelAttribute MemberUpdateRequest request,
                               RedirectAttributes rttr) {
        try {
            memberService.updateMember(request);

            rttr.addAttribute("alertCode", "MP200");
            return "redirect:/mypage";

        } catch (FeignException e) {
            FeignErrorParser.FeignError fe =
                    feignErrorParser.parse(e, "C002", "수정에 실패했습니다.");

            rttr.addAttribute("alertCode", fe.code());
            return "redirect:/mypage";

        } catch (Exception e) {
            rttr.addAttribute("alertCode", "C002");
            return "redirect:/mypage";
        }
    }

    @LoginRequired
    @PostMapping("/mypage/withdraw")
    public String withdrawMember(HttpServletResponse response, RedirectAttributes rttr) {
        try {
            memberService.withdrawMember();
            deleteCookie(response, "access-token");
            deleteCookie(response, "refresh-token");

            rttr.addAttribute("alertCode", "MP201"); // 예: 탈퇴 성공 코드
            return "redirect:/";

        } catch (FeignException e) {
            FeignErrorParser.FeignError fe =
                    feignErrorParser.parse(e, "C002", "탈퇴에 실패했습니다.");
            rttr.addAttribute("alertCode", fe.code());
            return "redirect:/mypage";

        } catch (Exception e) {
            rttr.addAttribute("alertCode", "C002");
            return "redirect:/mypage";
        }
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}