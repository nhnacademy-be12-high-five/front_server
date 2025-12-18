package com.example.high_five.controller.order;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.common.CustomPage;
import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.MyOrderResponse;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MyOrderController {

    private final FrontOrderService frontOrderService;
    private final MemberService memberService;

    @ModelAttribute("myInfo")
    public MemberResponse getMemberInfo() {
        try {
            var response = memberService.getMyInfo();
            if (response != null && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("회원 정보 로드 실패: {}", e.getMessage());
        }
        return null;
    }

    @LoginRequired
    @GetMapping("/mypage/orders")
    public String myOrders(@ModelAttribute("myInfo") MemberResponse myInfo,
                           Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {

        if (myInfo == null) {
            return "redirect:/member/login.html";
        }

        CommonPageResponse<MyOrderResponse> orderPage = frontOrderService.getMyOrders(myInfo.getMemberId(), page, size);

        model.addAttribute("orders", orderPage.getData());
        model.addAttribute("page", orderPage);
        model.addAttribute("currentTab", "orders");

        return "mypage/orders";
    }
}
