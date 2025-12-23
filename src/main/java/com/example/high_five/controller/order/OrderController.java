package com.example.high_five.controller.order;

import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import com.example.high_five.service.OrderClient.OrderCreateResponse;
import com.example.high_five.service.PaymentService;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final FrontOrderService frontOrderService;
    private final PaymentService paymentService;
    private final MemberService memberService;
    private final CouponService couponService;

    @Value("test_ck_d46qopOB8969zAlo4YJY3ZmM75y0")
    private String tossClientKey;


    @GetMapping("/sheet")
    public String orderSheet(@RequestParam List<Long> bookIds,
                             @RequestParam List<Integer> quantities,
                             @RequestHeader(value = "X-USER-ID", required = false) Long userId,
                             @RequestHeader(value = "Authorization", required = false) String token,
                             Model model) {


        OrderResponse orderSheet = frontOrderService.createOrderSheet(userId, token, bookIds, quantities);
        model.addAttribute("orderSheet", orderSheet);

        try {
            PointBalanceResponse pointResponse = memberService.getMyBalance(token).getBody();
            model.addAttribute("point", pointResponse.getCurrentPoint());
            if (userId == null) {
                userId = pointResponse.getMemberId();
            }
        } catch (Exception e) {
            model.addAttribute("point", 0);
        }

        try {
            List<MemberCouponResponseDto> coupons = couponService.getUsableCoupons(userId);
            model.addAttribute("coupons", coupons);
        } catch (Exception e) {
            model.addAttribute("coupons", Collections.emptyList());
        }
        model.addAttribute("userId", userId);

        try {
            List<PaymentMethodResponse> paymentMethods = paymentService.getAllMethods();
            model.addAttribute("paymentMethods", paymentMethods);
        } catch (Exception e) {
            log.warn("결제 수단 조회 실패", e);
            model.addAttribute("paymentMethods", List.of());
        }

        OrderCheckoutRequest checkoutRequest = new OrderCheckoutRequest();
        checkoutRequest.setReceiverName(orderSheet.getName());
        model.addAttribute("checkoutRequest", checkoutRequest);

        model.addAttribute("tossClientKey", tossClientKey);
        return "order/order";
    }

    @PostMapping(path = "/api/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createOrderApi(
            @Valid @ModelAttribute OrderCheckoutRequest request,
            BindingResult bindingResult,
            @RequestHeader(value = "X-USER-ID", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @CookieValue(value = "guestCookie", required = false) String guestId
    ) {

        Long userId = (headerUserId != null) ? headerUserId : paramUserId;

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("입력 정보가 올바르지 않습니다.");
        }

        try {
            if (userId != null) {
                request.setUserId(userId);
            }


            OrderCreateResponse response = frontOrderService.placeOrder(request, userId, guestId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            return ResponseEntity.badRequest().body("주문 실패: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String paymentKey,
                                 @RequestParam String orderId,
                                 @RequestParam Long amount,
                                 Model model) {
        log.info("결제 승인 요청: orderId={}, amount={}", orderId, amount);
        try {
            PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderKey(orderId)
                    .amount(amount)
                    .paymentMethod("TOSS")
                    .build();
            PaymentConfirmResponse response = paymentService.confirmPayment(confirmRequest);

            model.addAttribute("orderNumber", response.getPaymentId());
            model.addAttribute("totalPrice", response.getAmount());
            model.addAttribute("payMethodName", "Toss Payments");
            model.addAttribute("orderDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            model.addAttribute("orderId", orderId);

            return "order/payment-success";
        } catch (FeignException e) {
            log.error("결제 승인 실패 (Feign): status={}, body={}", e.status(), e.contentUTF8());
            model.addAttribute("code", "PAYMENT_CONFIRM_ERROR");
            model.addAttribute("message", "결제 승인 중 오류가 발생했습니다.");
            return "order/payment-fail";
        } catch (Exception e) {
            log.error("결제 시스템 오류", e);
            model.addAttribute("code", "SYSTEM_ERROR");
            model.addAttribute("message", "시스템 오류가 발생했습니다.");
            return "order/payment-fail";
        }
    }

    @GetMapping("/fail")
    public String paymentFail(@RequestParam(required = false) String code,
                              @RequestParam(required = false) String message,
                              Model model) {
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        return "order/payment-fail";
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        try {
            frontOrderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("주문 취소 API 처리 실패 (orderId={})", orderId, e);
            return ResponseEntity.ok().build();
        }
    }

    @PostMapping("/{orderId}/confirm")
    @ResponseBody
    public ResponseEntity<Void> confirmOrder(@PathVariable Long orderId) {
        try {
            frontOrderService.confirmOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("구매 확정 API 처리 실패 (orderId={})", orderId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/guest")
    public String getGuestOrder(@RequestParam Long orderId,
                                @RequestParam Integer password,
                                Model model) {
        try {
            OrderResponse orderResponse = frontOrderService.getGuestOrder(orderId, password);

            model.addAttribute("order", orderResponse);

            return "order/order-detail";

        } catch (Exception e) {
            log.warn("비회원 주문 조회 실패: {}", e.getMessage());
            // 실패 시 다시 로그인 페이지로 돌아가며 에러 메시지 표시
            model.addAttribute("error", "주문 정보가 일치하지 않거나 존재하지 않습니다.");
            return "member/login";
        }
    }
}