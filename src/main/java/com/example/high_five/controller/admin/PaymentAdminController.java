package com.example.high_five.controller.admin;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.payment.DailySalesResponse;
import com.example.high_five.dto.payment.MethodStatusRequest;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import com.example.high_five.dto.payment.PaymentStatsResponse;
import com.example.high_five.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminController {

    private final PaymentService paymentClient;

    @GetMapping
    @LoginRequired(adminOnly = true)
    public String getPaymentMethods(Model model) {
        try {
            List<PaymentMethodResponse> methods = paymentClient.getAllMethods();
            model.addAttribute("methods", methods);
        } catch (Exception e) {
            log.error("결제 수단 조회 실패", e);
            model.addAttribute("errorMessage", "결제 수단 목록을 불러오지 못했습니다.");
        }
        return "admin/payments"; // 템플릿 경로
    }

    @PostMapping("/{methodId}/status")
    @LoginRequired(adminOnly = true)
    public String updateStatus(@PathVariable Long methodId,
                               @RequestParam boolean isActive,
                               RedirectAttributes redirectAttributes) {
        try {
            paymentClient.updateStatus(methodId, new MethodStatusRequest(isActive));
            redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        } catch (Exception e) {
            log.error("상태 변경 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 실패에 실패했습니다.");
        }
        return "redirect:/admin/payments";
    }

    @GetMapping("/stats")
    @LoginRequired(adminOnly = true)
    public String paymentStatsPage(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        // 1. 전체 요약 통계 조회
        try {
            PaymentStatsResponse summary = paymentClient.getTotalStats();
            model.addAttribute("summary", summary);
        } catch (Exception e) {
            model.addAttribute("summary", new PaymentStatsResponse(0L, 0L, 0L, 0L, 0L, 0L));
        }

        // 2. 일별 매출 차트 데이터 조회 (기본값: 최근 7일)
        if (endDate == null) endDate = LocalDate.now();
        if (startDate == null) startDate = endDate.minusDays(6);

        try {
            List<DailySalesResponse> dailyStats = paymentClient.getDailyStats(startDate, endDate);
            model.addAttribute("dailyStats", dailyStats);
        } catch (Exception e) {
            model.addAttribute("dailyStats", List.of());
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/payment-stats";
    }
}