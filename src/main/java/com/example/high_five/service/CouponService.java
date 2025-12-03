package com.example.high_five.service;

import com.example.high_five.dto.coupon.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "gateway-server", contextId = "couponClient", url = "http://localhost:8082")
public interface CouponService {

    @GetMapping("/api/coupons/members/{memberId}")
    Map<String, Object> getMemberCoupons(@PathVariable("memberId") Long memberId,
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
    void issueCoupon(@RequestHeader("X-USER-ID") Long userId,
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
}
