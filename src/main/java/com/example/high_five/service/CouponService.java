package com.example.high_five.service;

import com.example.high_five.dto.coupon.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "GATEWAY", url = "http://127.0.0.1:10485")
public interface CouponService {

    @GetMapping("/api/coupons/members/{memberId}")
    Map<String, Object> getMemberCoupons(@PathVariable("memberId") Long memberId,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/api/admin/coupons")
    List<CouponTemplateDto> getAdminCoupons();

    @GetMapping("/api/coupons/templates")
    Map<String, Object> getIssuableCoupons(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size);

    @PostMapping("/api/admin/coupon-policy")
    void createCouponPolicy(@RequestBody CouponPolicyRequestDto dto);

    @PostMapping("/api/coupons/issue")
    void issueCoupon(@RequestHeader("X-USER-ID") Long userId,
                     @RequestBody UserCouponIssueRequestDto requestDto);

    @GetMapping("/api/admin/coupon-policy")
    List<CouponPolicyResponseDto> getAllPolicies();

    @PostMapping("/api/admin/coupons")
    void createCouponTemplate(@RequestBody CouponCreateRequestDto dto);

    @DeleteMapping("/api/admin/coupon-policy/{id}")
    void disableCouponPolicy(@PathVariable("id") Long id);
}
