package com.example.high_five.controller.admin;

import com.example.high_five.dto.payment.DailySalesResponse;
import com.example.high_five.dto.payment.PaymentStatsResponse;
import com.example.high_five.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final PaymentService paymentClient;

    @GetMapping("/stats")
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