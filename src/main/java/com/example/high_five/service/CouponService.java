package com.example.high_five.service;

import com.example.high_five.dto.coupon.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "TEAM5-GATEWAY-SERVER", contextId = "couponClient", url = "${gateway.uri}")
public interface CouponService {

    @GetMapping("/api/coupons/members")
    Map<String, Object> getMemberCoupons(@CookieValue("AccessToken") String accessToken,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/api/coupons/admin/coupons")
    List<CouponTemplateDto> getAdminCoupons();

    @GetMapping("/api/coupons/templates")
    Map<String, Object> getIssuableCoupons(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size);

    @PostMapping("/api/coupons/admin/coupon-policy")
    void createCouponPolicy(@RequestBody CouponPolicyRequestDto dto);

    @PostMapping("/api/coupons/issue")
    void issueCoupon(@CookieValue("AccessToken") String accessToken,
                     @RequestBody UserCouponIssueRequestDto requestDto);

    @GetMapping("/api/coupons/admin/coupon-policy")
    List<CouponPolicyResponseDto> getAllPolicies();

    @PostMapping("/api/coupons/admin/coupons")
    void createCouponTemplate(@RequestBody CouponCreateRequestDto dto);

    @DeleteMapping("/api/coupons/admin/coupon-policy/{id}")
    void disableCouponPolicy(@PathVariable("id") Long id);

    @DeleteMapping("/api/coupons/members/{memberId}/coupons/{memberCouponId}")
    void deleteMemberCoupon(@PathVariable("memberId") Long memberId,
                            @PathVariable("memberCouponId") Long memberCouponId);

    @PostMapping("/api/coupons/admin/member-coupons/issue")
    void issueCouponByAdmin(@RequestBody MemberCouponIssueRequestDto requestDto);

    @GetMapping("/api/coupons/admin/coupon-policy/{id}")
    CouponPolicyResponseDto getCouponPolicy(@PathVariable("id") Long id);
}
