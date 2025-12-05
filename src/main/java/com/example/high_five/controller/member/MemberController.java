package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.service.MemberService;
import java.util.Collections;
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
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        if (accessToken == null) return "redirect:/member/login.html";

        String authHeader = "Bearer " + accessToken;

        // 내 정보 조회
        try {
            var myInfo = memberService.getMyInfo(authHeader).getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (Exception e) {
            log.error("내 정보 조회 실패", e);
        }

        // 포인트 정보 조회
        try {
            // 잔액
            var balance = memberService.getMyBalance(authHeader).getBody();
            model.addAttribute("balance", balance);

            // 이력
            var historyPage = memberService.getMyHistory(authHeader, page, 10).getBody();
            if (historyPage != null) {
                model.addAttribute("histories", historyPage.getContent());
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", historyPage.getTotalPages());
            } else {
                model.addAttribute("histories", Collections.emptyList());
                model.addAttribute("totalPages", 0);
            }
            log.info("포인트 조회 성공:");

        } catch (Exception e) {
            log.error("포인트 조회 실패", e);
            model.addAttribute("balance", new PointBalanceResponse(0L, 99L, 99L));
            model.addAttribute("histories", Collections.emptyList());
            model.addAttribute("totalPages", 0);
        }

        model.addAttribute("currentTab", tab);
        return "member/mypage";
    }

}