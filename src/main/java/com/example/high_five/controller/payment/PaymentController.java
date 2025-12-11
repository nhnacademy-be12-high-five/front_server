package com.example.high_five.controller.payment;

import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.service.PaymentService; // Feign Client 인터페이스
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService; // Feign Client

    @Value("test_ck_d46qopOB8969zAlo4YJY3ZmM75y0")
    private String tossClientKey;

    // 결제 페이지 진입
    @GetMapping("/payment")
    public String checkoutPage(
            @RequestParam(required = false, defaultValue = "50000") Long amount,
            @RequestParam(required = false, defaultValue = "HIGH-FIVE 도서 외 1건") String orderName,
            @RequestParam(required = false) String orderId,
            Model model) {

        // orderId가 없으면 테스트용으로 생성 (실전 연결 전까지 유용)
        if (orderId == null) {
            orderId = "ORD-" + UUID.randomUUID().toString();
        }

        // 1. 필수 데이터 주입
        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        model.addAttribute("customerName", "홍길동"); // 실제 로그인 유저명으로 변경 필요

        // 2. 결제 수단 목록 (HTML 탭 렌더링용)
        List<Map<String, String>> methods = List.of(
                Map.of("key", "CARD", "label", "신용카드"),
                Map.of("key", "TOSS_PAY", "label", "토스페이"),
                Map.of("key", "VIRTUAL_ACCOUNT", "label", "가상계좌")
        );
        model.addAttribute("paymentMethods", methods);
        model.addAttribute("currentKey", "CARD"); // 기본 선택값

        return "order/payment"; // templates/order/payment.html
    }

    // [2] 토스 결제 성공 리다이렉트 처리
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {

        try {
            // 결제 승인 요청 (Feign -> Gateway -> Payment Server)
            PaymentConfirmRequest request = new PaymentConfirmRequest(paymentKey, orderId, amount, "TOSS");
            PaymentConfirmResponse response = paymentService.confirmPayment(request);

            // 성공 시 결과 페이지 데이터 전달
            model.addAttribute("orderNumber", response.getPaymentId());
            model.addAttribute("totalPrice", response.getAmount());
            model.addAttribute("payMethodName", "Toss Payments");
            model.addAttribute("orderDateTime", java.time.LocalDateTime.now());

            return "order/ordersuccess"; // templates/order/ordersuccess.html

        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            model.addAttribute("message", "결제 승인 중 오류가 발생했습니다.");
            model.addAttribute("code", "CONFIRM_ERROR");
            return "order/fail";
        }
    }

    // [3] 토스 결제 실패 리다이렉트 처리
    @GetMapping("/payment/fail")
    public String paymentFail(
            @RequestParam String code,
            @RequestParam String message,
            Model model) {

        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "order/fail"; // templates/order/fail.html
    }
}