package com.example.high_five.controller.admin;

import com.example.high_five.dto.coupon.*;
import com.example.high_five.dto.point.PointAdminAdjustmentRequest;
import com.example.high_five.dto.point.PointAdminPolicyRequest;
import com.example.high_five.dto.point.PointAdminPolicyResponse;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.MemberService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final CouponService couponService;
    private final MemberService memberService;

    @GetMapping("/api/coupons/admin/coupons")
    public String couponPage(Model model){
        List<CouponTemplateDto> coupons = couponService.getAdminCoupons();
        List<CouponPolicyResponseDto> policies = couponService.getAllPolicies();
        model.addAttribute("coupons",coupons);
        model.addAttribute("policies",policies);
        return "admin/coupons";
    }

    @PostMapping("/api/coupons/admin/policy/create")
    public String createPolicy(@ModelAttribute CouponPolicyRequestDto dto) {
        couponService.createCouponPolicy(dto);

        return "redirect:/api/coupons/admin/coupons";
    }

    @PostMapping("/api/coupons/admin/coupons/create")
    public String createCouponTemplate(@ModelAttribute CouponCreateRequestDto dto, RedirectAttributes redirectAttributes) {
        try {
            couponService.createCouponTemplate(dto);
            redirectAttributes.addFlashAttribute("message", "쿠폰 템플릿이 성공적으로 생성되었습니다.");
        } catch (FeignException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "템플릿 생성 실패: 정책이 비활성화 상태이거나 잘못된 요청입니다.");
        }
        return "redirect:/api/coupons/admin/coupons";
    }

    @PostMapping("/api/coupons/admin/policy/{id}")
    public String disablePolicy(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.disableCouponPolicy(id);
            redirectAttributes.addFlashAttribute("message", "정책이 비활성화되었으며, 관련 쿠폰이 모두 만료 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "정책 비활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/api/coupons/admin/coupons";
    }

    @PostMapping("/api/coupons/admin/issue-manual")
    public String issueCouponManually(@RequestParam Long userId,
                                      @RequestParam Long couponId,
                                      RedirectAttributes redirectAttributes) {
        try {
            MemberCouponIssueRequestDto requestDto = new MemberCouponIssueRequestDto(couponId, userId);
            couponService.issueCouponByAdmin(requestDto);

            redirectAttributes.addFlashAttribute("message", "회원(" + userId + ")에게 쿠폰이 정상적으로 지급되었습니다.");
        } catch (FeignException e) {
            String serverMessage = e.contentUTF8();

            if (e.status() == 400 && serverMessage != null) {
                // "정책 중단" 또는 "잘못된 요청" 메시지 전달
                redirectAttributes.addFlashAttribute("errorMessage", serverMessage);
            } else if (e.status() == 409) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 해당 쿠폰을 보유한 회원입니다.");
            } else if (e.status() == 404) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 회원 또는 쿠폰입니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "쿠폰 지급 실패 (오류: " + e.status() + ")");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "시스템 오류가 발생했습니다.");
        }

        return "redirect:/api/coupons/admin/coupons";
    }

    @GetMapping("/api/admin/points/policy")
    public String getPointPage(Model model) {
        try {
            PointAdminPolicyResponse policy = memberService.getPolicy().getBody();
            model.addAttribute("policy", policy);
        } catch (Exception e) {
            log.error("포인트 정책 조회 실패", e);
            model.addAttribute("policy", new PointAdminPolicyResponse(0, 0, 0));
            model.addAttribute("errorMessage", "포인트 정책을 불러오는데 실패했습니다.");
        }
        return "admin/points";
    }

    @PostMapping("/api/admin/points/policy")
    public String updatePointPolicy(@ModelAttribute PointAdminPolicyRequest request,
                                    RedirectAttributes redirectAttributes) {
        try {
            memberService.updatePolicy(request);
            redirectAttributes.addFlashAttribute("message", "포인트 정책이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            log.error("포인트 정책 수정 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "정책 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/api/admin/points";
    }

    @PostMapping("/api/admin/points/adjustment")
    public String adjustmentMemberPoint(@ModelAttribute PointAdminAdjustmentRequest request,
                                        RedirectAttributes redirectAttributes) {
        try {
            memberService.manualAdjustment(request);

            String action = request.getAmount() > 0 ? "지급" : "차감";
            redirectAttributes.addFlashAttribute("message",
                    String.format("회원(%d)에게 포인트 %s 처리가 완료되었습니다.", request.getMemberId(), action));

        } catch (FeignException e) {
            if (e.status() == 404) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 회원입니다.");
            } else if (e.status() == 400) {
                redirectAttributes.addFlashAttribute("errorMessage", "잘못된 요청입니다. (잔액 부족 등)");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "포인트 조정 실패 (Error: " + e.status() + ")");
            }
        } catch (Exception e) {
            log.error("포인트 수동 조정 시스템 오류", e);
            redirectAttributes.addFlashAttribute("errorMessage", "시스템 오류가 발생했습니다.");
        }

        return "redirect:/api/admin/points";
    }

}
