package com.example.high_five.controller.order;

import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    @GetMapping("/mypage")
    public String myPage(@RequestParam(value = "tab", required = false) String tab, Model model) {
        Long testUserId = 1L;
        
        // 쿠폰 서버에 요청을 보내 데이터 가져오기
        List<MemberCouponResponseDto> coupons = Collections.emptyList();

        try {
            // 2. 쿠폰 서버 호출 (Page 객체가 Map 형태로 반환됨)
            Map<String, Object> response = couponService.getMemberCoupons(testUserId, 0, 10);

            // 3. "content" 필드에서 리스트 추출 및 DTO 변환
            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

                coupons = content.stream()
                        .map(item -> objectMapper.convertValue(item, MemberCouponResponseDto.class))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("쿠폰 서버 통신 오류: " + e.getMessage());
            // 오류 발생 시 빈 리스트 유지
        }
        
        // 모델에 담아서 타임리프로 전달
        model.addAttribute("myCoupons", coupons);
        model.addAttribute("couponCount", coupons.size());
        model.addAttribute("currentTab", tab != null ? tab : "info");
        
        return "order/mypage";
    }
}