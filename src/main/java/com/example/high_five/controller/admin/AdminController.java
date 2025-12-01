package com.example.high_five.controller.admin;

import com.example.high_five.dto.coupon.CouponCreateRequestDto;
import com.example.high_five.dto.coupon.CouponPolicyRequestDto;
import com.example.high_five.dto.coupon.CouponPolicyResponseDto;
import com.example.high_five.dto.coupon.CouponTemplateDto;
import com.example.high_five.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final CouponService couponService;

    @GetMapping("/admin/coupons.html")
    public String couponPage(Model model){
        List<CouponTemplateDto> coupons = couponService.getAdminCoupons();
        List<CouponPolicyResponseDto> policies = couponService.getAllPolicies();
        model.addAttribute("coupons",coupons);
        model.addAttribute("policies",policies);
        return "admin/coupons";
    }

    @PostMapping("/admin/policy/create")
    public String createPolicy(@ModelAttribute CouponPolicyRequestDto dto) {
        couponService.createCouponPolicy(dto);

        return "redirect:/admin/coupons.html";
    }

    @PostMapping("/admin/coupons/create")
    public String createCouponTemplate(@ModelAttribute CouponCreateRequestDto dto) {
        // 폼 데이터를 받아 쿠폰 서버로 전송
        couponService.createCouponTemplate(dto);
        return "redirect:/admin/coupons.html";
    }
}
