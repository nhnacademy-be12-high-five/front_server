package com.example.high_five.controller.payment;

import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import com.example.high_five.service.PaymentService; // Feign Client 인터페이스
import feign.FeignException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
            @RequestParam(required = false, defaultValue = "고객") String customerName,
            @RequestParam(required = false) String customerEmail,
            Model model) {

        // orderId가 없으면 테스트용으로 생성
        if (orderId == null) {
            orderId = UUID.randomUUID().toString();
            log.warn("테스트 모드: 임시 orderId 생성 = {}", orderId);
        }

        // 백엔드에서 활성화된 결제 수단 가져오기
        List<PaymentMethodResponse> paymentMethods;
        try {
            paymentMethods = paymentService.getActiveMethods();
        } catch (Exception e) {
            log.error("결제 수단 조회 실패", e);
            // 실패 시 보여줄 기본값 (DTO 구조와 맞춰야 HTML 에러 안 남)
            paymentMethods = List.of(
                    PaymentMethodResponse.builder().name("TOSS").alias("통합결제").isActive(true).build()
            );
        }

        model.addAttribute("paymentMethods", paymentMethods);

        String defaultMethod = paymentMethods.isEmpty() ? "" : paymentMethods.getFirst().getName();
        model.addAttribute("currentKey", defaultMethod);

        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderName", orderName);
        model.addAttribute("amount", amount);
        model.addAttribute("customerName", customerName);
        model.addAttribute("customerEmail", customerEmail);

        return "order/payment";
    }

    // 토스 결제 성공 -> 백엔드 승인 요청
    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            Model model) {

        try {
            log.info("결제 승인 요청 시작: orderId={}, amount={}", orderId, amount);

            // 1. Gateway -> Payment Server 승인 요청
            PaymentConfirmRequest request = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderKey(orderId) // Toss의 orderId = 우리의 orderKey
                    .amount(amount)
                    .paymentMethod("TOSS")
                    .build();

            PaymentConfirmResponse response = paymentService.confirmPayment(request);

            // 2. 성공 시 데이터 전달
            model.addAttribute("orderNumber", response.getPaymentId()); // 또는 주문번호
            model.addAttribute("totalPrice", response.getAmount());
            model.addAttribute("payMethodName", "Toss Payments");
            model.addAttribute("orderDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            // 주문 상세 페이지로 갈 링크를 위해 orderId도 전달
            model.addAttribute("orderId", orderId);

            return "order/ordersuccess";

        } catch (FeignException e) {
            log.error("Payment Server 승인 실패: status={}, body={}", e.status(), e.contentUTF8());
            model.addAttribute("code", "PAYMENT_CONFIRM_ERROR");
            model.addAttribute("message", "결제 승인 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "order/fail";

        } catch (Exception e) {
            log.error("결제 시스템 오류", e);
            model.addAttribute("code", "SYSTEM_ERROR");
            model.addAttribute("message", "시스템 오류가 발생했습니다.");
            return "order/fail";
        }
    }

    // [3] 토스 결제 실패 리다이렉트 처리
    @GetMapping("/payment/fail")
    public String paymentFail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            Model model) {

        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "order/fail";
    }
}