package com.example.high_five.controller.admin;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.point.PointAdminAdjustmentRequest;
import com.example.high_five.dto.point.PointAdminPolicyRequest;
import com.example.high_five.dto.point.PointAdminPolicyResponse;
import com.example.high_five.service.MemberService;
import feign.FeignException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/points")
public class PointAdminController {

    private final MemberService memberService;

    @GetMapping("/policy")
    @LoginRequired(adminOnly = true)
    public String getPointPage(Model model) {
        try {
            PointAdminPolicyResponse policy = memberService.getPolicy().getBody();
            model.addAttribute("policy", policy);
        } catch (Exception e) {
            log.error("포인트 정책 조회 실패", e);
            model.addAttribute("policy", new PointAdminPolicyResponse(0, 0, 0, LocalDateTime.now()));
            model.addAttribute("errorMessage", "포인트 정책을 불러오는데 실패했습니다.");
        }
        return "admin/points";
    }

    @PostMapping("/policy")
    @LoginRequired(adminOnly = true)
    public String updatePointPolicy(@ModelAttribute PointAdminPolicyRequest request, RedirectAttributes redirectAttributes) {
        try {
            memberService.updatePolicy(request);
            redirectAttributes.addFlashAttribute("message", "포인트 정책이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            log.error("포인트 정책 수정 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "정책 수정 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/points/policy";
    }

    @PostMapping("/adjustment")
    @LoginRequired(adminOnly = true)
    public String adjustmentMemberPoint(@ModelAttribute PointAdminAdjustmentRequest request, RedirectAttributes redirectAttributes) {
        try {
            memberService.manualAdjustment(request);

            String action = request.getAmount() > 0 ? "지급" : "차감";
            redirectAttributes.addFlashAttribute("message2", String.format("포인트 %s 처리가 완료되었습니다.", action));

        } catch (FeignException e) {
            if (e.status() == 404) {
                redirectAttributes.addFlashAttribute("errorMessage2", "존재하지 않는 회원입니다.");
            } else if (e.status() == 400) {
                redirectAttributes.addFlashAttribute("errorMessage2", "잘못된 요청입니다. (잔액 부족 등)");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage2", "포인트 조정 실패 (Error: " + e.status() + ")");
            }
        } catch (Exception e) {
            log.error("포인트 수동 조정 시스템 오류", e);
            redirectAttributes.addFlashAttribute("errorMessage2", "시스템 오류가 발생했습니다.");
        }

        return "redirect:/admin/points/policy";
    }

    @GetMapping
    @LoginRequired(adminOnly = true)
    public String index() {
        return "redirect:/admin/points/policy";
    }

}
