package com.example.high_five.service;

import com.example.high_five.dto.coupon.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "gateway-server", contextId = "couponClient", url = "${gateway.uri}")
public interface CouponService {

    @GetMapping("/api/coupons/members")
    Map<String, Object> getMemberCoupons(@CookieValue("access-token") String accessToken,
                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size);

    @GetMapping("/api/coupons/admin/coupons")
    List<CouponTemplateDto> getAdminCoupons();

    @GetMapping("/api/coupons/templates")
    Map<String, Object> getIssuableCoupons(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size);

    @PostMapping("/api/coupons/admin/coupon-policies")
    void createCouponPolicy(@RequestBody CouponPolicyRequestDto dto);

    @PostMapping("/api/coupons/issue")
    void issueCoupon(@CookieValue("access-token") String accessToken,
                     @RequestBody UserCouponIssueRequestDto requestDto);

    @GetMapping("/api/coupons/admin/coupon-policies")
    List<CouponPolicyResponseDto> getAllPolicies();

    @PostMapping("/api/coupons/admin/coupons")
    void createCouponTemplate(@RequestBody CouponCreateRequestDto dto);

    @PostMapping("/api/coupons/admin/coupons/{couponId}/change-status")
    void updateCouponStatus(@PathVariable("couponId") Long couponId, @RequestBody CouponStatusRequestDto requestDto);

    @DeleteMapping("/api/coupons/admin/coupon-policies/{id}")
    void disableCouponPolicy(@PathVariable("id") Long id);

    @DeleteMapping("/api/coupons/members/{memberId}/coupons/{memberCouponId}")
    void deleteMemberCoupon(@PathVariable("memberId") Long memberId,
                            @PathVariable("memberCouponId") Long memberCouponId);

    @PostMapping("/api/coupons/admin/member-coupons/issue")
    void issueCouponByAdmin(@RequestBody MemberCouponIssueRequestDto requestDto);

    @GetMapping("/api/coupons/admin/coupon-policies/{id}")
    CouponPolicyResponseDto getCouponPolicy(@PathVariable("id") Long id);

    @GetMapping("/api/coupons/books/{book-id}")
    List<CouponTemplateDto> getBookCoupons(@PathVariable("book-id") Long bookId,
                                           @RequestParam("category-ids") List<Long> categoryIds,
                                           @RequestParam("include-global") boolean includeGlobal);

    @GetMapping("/api/coupons/members/order")
    List<MemberCouponResponseDto> getUsableCoupons(@RequestHeader(name = "X-USER-ID", required = false) Long memberId,
                                                   @RequestParam("bookIds") List<Long> bookIds);
}
