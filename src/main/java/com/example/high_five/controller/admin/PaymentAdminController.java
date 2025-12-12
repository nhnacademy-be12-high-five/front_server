package com.example.high_five.controller.admin;

import com.example.high_five.dto.payment.MethodStatusRequest;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import com.example.high_five.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/payments")
public class PaymentAdminController {

    private final PaymentService paymentService;

    @GetMapping
    public String getPaymentMethods(Model model) {
        try {
            List<PaymentMethodResponse> methods = paymentService.getAllMethods();
            model.addAttribute("methods", methods);
        } catch (Exception e) {
            log.error("결제 수단 조회 실패", e);
            model.addAttribute("errorMessage", "결제 수단 목록을 불러오지 못했습니다.");
        }
        return "admin/payments"; // 템플릿 경로
    }

    @PostMapping("/{methodId}/status")
    public String updateStatus(@PathVariable Long methodId,
                               @RequestParam boolean active,
                               RedirectAttributes redirectAttributes) {
        try {
            paymentService.updateStatus(methodId, new MethodStatusRequest(active));
            redirectAttributes.addFlashAttribute("message", "상태가 변경되었습니다.");
        } catch (Exception e) {
            log.error("상태 변경 실패", e);
            redirectAttributes.addFlashAttribute("errorMessage", "상태 변경 실패");
        }
        return "redirect:/admin/payments";
    }
}