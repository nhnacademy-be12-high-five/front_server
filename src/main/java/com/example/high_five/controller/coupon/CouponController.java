package com.example.high_five.controller.coupon;

import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.dto.coupon.UserCouponIssueRequestDto;
import com.example.high_five.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final ObjectMapper objectMapper;

    @GetMapping("/coupon")
    public String couponRegisterPage(Model model){
        List<CouponTemplateDto> templates = Collections.emptyList();

        try {
            // 1. 쿠폰 서버에서 발급 가능한 쿠폰 목록 조회 (Page 객체 -> Map)
            Map<String, Object> response = couponService.getIssuableCoupons(0, 20);

            // 2. "content" 필드 추출 및 DTO 변환
            if (response != null && response.containsKey("content")) {
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

                templates = content.stream()
                        .map(item -> objectMapper.convertValue(item, CouponTemplateDto.class))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("쿠폰 서버 통신 오류 (발급존): " + e.getMessage());
        }

        model.addAttribute("templates", templates);
        return "order/coupon-register";
    }

    @PostMapping("/coupon/issue")
    public String issueCoupon(@RequestParam Long couponId, RedirectAttributes redirectAttributes) {
        // 1. 임시 사용자 ID (로그인 구현 전이므로 1번 사용자로 고정)
        Long userId = 1L;

        try {
            UserCouponIssueRequestDto requestDto = new UserCouponIssueRequestDto(couponId);

            couponService.issueCoupon(userId, requestDto);

            redirectAttributes.addFlashAttribute("message", "쿠폰이 성공적으로 발급되었습니다.");

        } catch (FeignException e) {
            String serverMessage = e.contentUTF8();

            if (e.status() == 409) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 해당 쿠폰을 발급받으셨습니다.");
            } else if (e.status() == 400) {
                if (serverMessage != null && !serverMessage.isBlank()) {
                    redirectAttributes.addFlashAttribute("errorMessage", serverMessage);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "잘못된 요청입니다.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "쿠폰 발급에 실패했습니다. (오류: " + e.status() + ")");
            }
            System.err.println("쿠폰 발급 실패: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            System.err.println("쿠폰 발급 실패: " + e.getMessage());
        }
        return "redirect:/mypage?tab=coupons";
    }
}
