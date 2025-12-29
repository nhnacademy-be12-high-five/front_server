package com.example.high_five.controller.order;

import com.example.high_five.dto.coupon.MemberCouponResponseDto;
import com.example.high_five.dto.member.response.AddressListResponse;
import com.example.high_five.dto.member.response.AddressResponse;
import com.example.high_five.dto.order.GuestOrderDetailResponse;
import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderCreateResponse;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.service.AddressService;
import com.example.high_five.service.CouponService;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.MemberService;
import com.example.high_five.service.PaymentService;
import feign.FeignException;
import jakarta.validation.Valid;
import java.util.Map;
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
    private final AddressService addressService;

    @Value("test_ck_d46qopOB8969zAlo4YJY3ZmM75y0")
    private String tossClientKey;


    @GetMapping("/sheet")
    public String orderSheet(@RequestParam List<Long> bookIds,
                             @RequestParam List<Integer> quantities,
                             @RequestHeader(value = "X-USER-ID", required = false) Long userId,
                             @RequestHeader(value = "Authorization", required = false) String token,
                             Model model) {

        // 주문 시트
        OrderResponse orderSheet =
                frontOrderService.createOrderSheet(userId, token, bookIds, quantities);
        model.addAttribute("orderSheet", orderSheet);

        // 포인트
        Long point = 0L;
        try {
            PointBalanceResponse pointResponse = memberService.getMyBalance(token).getBody();
            if (pointResponse != null) {
                point = pointResponse.getCurrentPoint();
                if (userId == null) {
                    userId = pointResponse.getMemberId();
                }
            }
        } catch (Exception ignore) {
        }
        model.addAttribute("point", point);
        model.addAttribute("userId", userId);

        // 쿠폰
        List<MemberCouponResponseDto> coupons = Collections.emptyList();
        if (userId != null) {
            try {
                coupons = couponService.getUsableCoupons(userId, bookIds);
            } catch (Exception ignore) {
            }
        }
        model.addAttribute("coupons", coupons);

        // 결제 수단
        List<PaymentMethodResponse> paymentMethods = Collections.emptyList();
        try {
            paymentMethods = paymentService.getAllMethods();
        } catch (Exception e) {
            log.warn("결제 수단 조회 실패", e);
        }
        model.addAttribute("paymentMethods", paymentMethods);

        // 배송지
        List<AddressResponse> addresses = Collections.emptyList();
        if (userId != null) {
            try {
                AddressListResponse response =
                        addressService.getAddressList().getBody();
                if (response != null && response.getAddressList() != null) {
                    addresses = response.getAddressList();
                }
            } catch (Exception e) {
                log.warn("배송지 목록 조회 실패", e);
            }
        }
        model.addAttribute("savedAddresses", addresses);

        // 기본 CheckoutRequest
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
                                 @RequestParam("orderId") String orderKey,
                                 @RequestParam Long amount,
                                 Model model) {
        log.info("결제 승인 요청: orderKey={}, amount={}", orderKey, amount);
        try {
            PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                    .paymentKey(paymentKey)
                    .orderKey(orderKey)
                    .amount(amount)
                    .paymentMethod("TOSS")
                    .build();

            PaymentConfirmResponse response = paymentService.confirmPayment(confirmRequest);

            model.addAttribute("orderId", response.getOrderId());

            // 기존 코드 유지
            model.addAttribute("totalPrice", response.getAmount());
            model.addAttribute("payMethodName", "Toss Payments");
            model.addAttribute("orderDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

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

    // [추가] 비회원 로그인 검증용 API (페이지 이동 없음, 확인만 함)
    @PostMapping("/guest/validate")
    @ResponseBody // 뷰가 아니라 데이터를 반환
    public ResponseEntity<?> validateGuestOrder(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String password = (String) request.get("password");

        try {
            // 서비스 호출해서 조회 되는지 확인만 해봄
            frontOrderService.getGuestOrder(orderId, password);

            // 에러 안 나면 성공
            return ResponseEntity.ok().body(Map.of("success", true));

        } catch (Exception e) {
            log.warn("비회원 검증 실패: {}", e.getMessage());
            // 실패 시 400 에러와 메시지 반환
            return ResponseEntity.badRequest().body(Map.of("message", "주문 정보가 일치하지 않습니다."));
        }
    }

    // [기존 유지] 실제 페이지 이동은 여기서 처리 (HTML 반환)
    @PostMapping("/guest")
    public String getGuestOrder(@RequestParam Long orderId,
                                @RequestParam String password,
                                Model model) {
        try {
            GuestOrderDetailResponse response = frontOrderService.getGuestOrder(orderId, password);
            model.addAttribute("order", response);
            return "order/guest-order-detail";
        } catch (Exception e) {
            // 혹시라도 여기서 에러나면 로그인 페이지로 (JS 검증 통과했으면 여긴 거의 안 옴)
            return "redirect:/member/login?error=true";
        }
    }
}