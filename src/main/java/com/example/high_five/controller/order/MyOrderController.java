package com.example.high_five.controller.order;

import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.order.MyOrderResponse;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MyOrderController {

    private final FrontOrderService frontOrderService;
    private final MemberService memberService;

    @GetMapping("/mypage/orders")
    public String myOrders(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        try {
            // 내 정보 조회 (UserID 획득용)
            var myInfo = memberService.getMyInfo().getBody();
            model.addAttribute("myInfo", myInfo);

            if (myInfo != null) {
                // 주문 내역 조회
                CustomPage<MyOrderResponse> orderPage = frontOrderService.getMyOrders(myInfo.getMemberId(), page, size);
                model.addAttribute("orders", orderPage.getContent());
                model.addAttribute("page", orderPage);
            }
        } catch (Exception e) {
            log.error("마이페이지 주문 내역 로드 실패", e);
            return "redirect:/member/login.html";
        }

        model.addAttribute("currentTab", "orders");
        return "mypage/orders";
    }
}
