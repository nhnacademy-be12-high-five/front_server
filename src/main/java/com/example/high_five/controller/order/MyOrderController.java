package com.example.high_five.controller.order;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.common.CustomPage;
import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.order.MyOrderResponse;
import com.example.high_five.dto.order.OrderReturnRequest;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
                           @RequestParam(defaultValue = "5") int size) {

        if (myInfo == null) {
            return "redirect:/member/login.html";
        }

        CommonPageResponse<MyOrderResponse> orderPage = frontOrderService.getMyOrders(myInfo.getMemberId(), page, size);

        model.addAttribute("orders", orderPage.getData());
        model.addAttribute("page", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentTab", "orders");

        return "mypage/orders";
    }

    @PostMapping("/mypage/orders/{orderId}/return")
    @ResponseBody // AJAX 요청에 대한 응답
    public ResponseEntity<String> requestReturn(@PathVariable Long orderId,
                                                @RequestBody OrderReturnRequest request) {
        try {
            frontOrderService.requestReturn(orderId, request);
            return ResponseEntity.ok("반품 신청이 완료되었습니다.");
        } catch (Exception e) {
            log.error("반품 신청 실패", e);
            return ResponseEntity.badRequest().body("반품 신청 실패: " + e.getMessage());
        }
    }
}
