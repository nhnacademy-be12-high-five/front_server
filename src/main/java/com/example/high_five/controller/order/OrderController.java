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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Spring Boot 3.x (javax -> jakarta)
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

    /**
     * 1. 주문서 작성 페이지 (GET)
     * - 장바구니나 상세페이지에서 넘어온 요청 처리
     */
    @GetMapping("/sheet")
    public String orderSheet(@RequestParam List<Long> bookIds,
                             @RequestParam List<Integer> quantities,
                             @RequestHeader(value = "X-USER-ID", required = false) Long userId,
                             @RequestHeader(value = "Authorization", required = false) String token,
                             Model model) {

        // 서비스에서 필요한 모든 데이터(상품, 회원, 쿠폰 등)를 조회하여 DTO로 반환
        OrderResponse orderSheet = frontOrderService.createOrderSheet(userId, token, bookIds, quantities);
        model.addAttribute("orderSheet", orderSheet);


        // 1. 포인트 조회
        try {
            PointBalanceResponse pointResponse = memberService.getMyBalance(token).getBody();
            model.addAttribute("point", pointResponse.getCurrentPoint());
        } catch (Exception e) {
            model.addAttribute("point", 0);
        }

        // 2. 사용 가능 쿠폰 조회
        try {
            List<MemberCouponResponseDto> coupons = couponService.getUsableCoupons(userId);
            model.addAttribute("coupons", coupons);
        } catch (Exception e) {
            model.addAttribute("coupons", Collections.emptyList());
        }

        // 3. 결제 수단 조회
        try {
            List<PaymentMethodResponse> paymentMethods = paymentService.getAllMethods();
            model.addAttribute("paymentMethods", paymentMethods);
        } catch (Exception e) {
            log.warn("결제 수단 조회 실패", e);
            model.addAttribute("paymentMethods", List.of());
        }


        // 폼 바인딩용 객체 초기화
        OrderCheckoutRequest checkoutRequest = new OrderCheckoutRequest();
        checkoutRequest.setReceiverName(orderSheet.getName());
        // checkoutRequest.setReceiverPhone(orderSheet.getPhoneNumber());
        // checkoutRequest.setReceiverAddress(orderSheet.getAddress()); // 주소 필드 있다면 추가

        model.addAttribute("checkoutRequest", checkoutRequest);

        // Toss Client Key 전달 (HTML에서 사용)
        model.addAttribute("tossClientKey", tossClientKey);
        return "order/order";
    }

    /**
     * 2. (AJAX) 주문 생성 API
     * - '결제하기' 버튼 클릭 시 호출됨. DB에 주문을 생성하고 orderId를 반환함.
     */
    @PostMapping(path = "/api/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createOrderApi(@Valid @ModelAttribute OrderCheckoutRequest request,
                                            BindingResult bindingResult,
                                            @RequestHeader(value = "X-USER-ID", required = false) Long userId) {

        if (bindingResult.hasErrors()) {
            log.warn("Order validation error: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body("입력 정보가 올바르지 않습니다.");
        }

        try {
            // 주문 생성 서비스 호출 -> OrderCreateResponse(orderId, orderKey, totalAmount) 반환
            OrderCreateResponse response = frontOrderService.placeOrder(request, userId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생", e);
            return ResponseEntity.badRequest().body("주문 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 3. 결제 성공 리다이렉트 및 최종 승인 (GET)
     * - Toss Payments 창에서 결제가 성공하면 이 URL로 리다이렉트됨
     * - 여기서 반드시 '승인 요청(confirm)'을 해야 함
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String paymentKey,
                                 @RequestParam String orderId,
                                 @RequestParam Long amount,
                                 Model model) {

        log.info("결제 승인 요청: orderId={}, amount={}", orderId, amount);

        try {
            // 3-1. Payment Server에 승인 요청
            PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderKey(orderId)
                    .amount(amount)
                    .paymentMethod("TOSS") // 필요 시 동적으로 변경
                    .build();

            PaymentConfirmResponse response = paymentService.confirmPayment(confirmRequest);

            // 3-2. 승인 성공 시 완료 페이지에 보여줄 데이터 세팅
            model.addAttribute("orderNumber", response.getPaymentId());
            model.addAttribute("totalPrice", response.getAmount());
            model.addAttribute("payMethodName", "Toss Payments"); // 혹은 response에서 받은 method
            model.addAttribute("orderDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            model.addAttribute("orderId", orderId); // 주문 상세보기 용도

            return "order/payment-success";

        } catch (FeignException e) {
            log.error("결제 승인 실패 (Feign): status={}, body={}", e.status(), e.contentUTF8());
            model.addAttribute("code", "PAYMENT_CONFIRM_ERROR");
            model.addAttribute("message", "결제 승인 중 오류가 발생했습니다. (" + e.status() + ")");
            return "order/payment-fail";

        } catch (Exception e) {
            log.error("결제 시스템 오류", e);
            model.addAttribute("code", "SYSTEM_ERROR");
            model.addAttribute("message", "시스템 오류가 발생했습니다.");
            return "order/payment-fail";
        }
    }

    /**
     * 4. 결제 실패 리다이렉트 (GET)
     * - Toss Payments 창에서 결제 실패/취소 시 이동
     */
    @GetMapping("/fail")
    public String paymentFail(@RequestParam(required = false) String code,
                              @RequestParam(required = false) String message,
                              Model model) {

        // 에러 코드와 메시지를 뷰에 전달
        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "order/payment-fail";
    }
}