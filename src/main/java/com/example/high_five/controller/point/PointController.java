package com.example.high_five.controller.point;

import com.example.high_five.dto.coupon.PointBalanceResponse;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/points")
public class PointController {

    private final MemberService memberService;

    @GetMapping
    public String pointPage(Model model, @RequestParam(defaultValue = "0") int page) {
        // 임시 유저 ID
        Long memberId = 1L;

        // 잔액 조회 API
        PointBalanceResponse balance = memberService.getMyBalance(memberId).getBody();

        // 내역 조회 API
        var history = memberService.getMyHistory(memberId, page, 10).getBody();

        model.addAttribute("balance", balance);
        model.addAttribute("histories", history.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", history.getTotalPages());
        model.addAttribute("currentTab", "points");


        return "mypage"; // HTML 파일 이름
    }
}
