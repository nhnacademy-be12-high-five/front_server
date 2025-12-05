package com.example.high_five.controller.point;

import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.dto.point.PointHistoryResponse;
import com.example.high_five.service.MemberService;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/points")
public class PointController {

    private final MemberService memberService;

    @GetMapping
    public String pointPage(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @CookieValue(value = "access-token", required = false) String accessToken) {

        if (accessToken == null) return "redirect:/member/login.html";
        String authHeader = "Bearer " + accessToken;

        try {
            PointBalanceResponse balance = memberService.getMyBalance(authHeader).getBody();
            CustomPage<PointHistoryResponse> historyPage = memberService.getMyHistory(authHeader, page, 10).getBody();

            model.addAttribute("balance", balance);
            model.addAttribute("histories", Objects.requireNonNull(historyPage).getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", historyPage.getTotalPages());

        } catch (Exception e) {
            model.addAttribute("balance", new PointBalanceResponse(0L, 0L, 0L));
            model.addAttribute("histories", Collections.emptyList());
        }

        model.addAttribute("currentTab", "points");

        return "member/mypage";
    }
}
