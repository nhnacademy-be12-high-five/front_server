package com.example.high_five.controller.order;

import com.example.high_five.dto.order.OrderCheckoutRequest;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.service.FrontOrderService;
import com.example.high_five.service.OrderClient.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // 폼 바인딩용 객체 초기화
        OrderCheckoutRequest checkoutRequest = new OrderCheckoutRequest();
        checkoutRequest.setReceiverName(orderSheet.getName());
        // checkoutRequest.setReceiverAddress(orderSheet.getAddress()); // 주소 필드 있다면 추가

        model.addAttribute("checkoutRequest", checkoutRequest);

        return "order/order";
    }

    /**
     * 2. 주문 생성 요청 (POST)
     * - 결제하기 버튼 클릭 시 실행
     */
    @PostMapping
    public String createOrder(@Valid @ModelAttribute OrderCheckoutRequest request,
                              BindingResult bindingResult,
                              @RequestHeader(value = "X-USER-ID", required = false) Long userId,
                              Model model) {

        // 2-1. 유효성 검사 실패 시 다시 주문서 페이지로 (에러 메시지 포함)
        if (bindingResult.hasErrors()) {
            log.warn("Validation Error: {}", bindingResult.getAllErrors());
            // 실제로는 다시 orderSheet 데이터를 로드해서 보여줘야 함
            // 간략하게 에러 페이지나 메시지를 전달
            return "redirect:/orders/sheet?error=validation";
        }

        try {
            // 2-2. 서비스 호출 (구체적인 DTO 반환)
            OrderCreateResponse response = frontOrderService.placeOrder(request, userId);

            // 2-3. 성공 시 완료 페이지로 리다이렉트
            return "redirect:/orders/success?orderId=" + response.getOrderId()
                    + "&amount=" + response.getTotalAmount();

        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            model.addAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "error/error"; // 공통 에러 페이지
        }
    }

    /**
     * 3. 주문 완료 페이지 (GET)
     */
    @GetMapping("/success")
    public String orderSuccess(@RequestParam Long orderId,
                               @RequestParam Integer amount,
                               Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        // 필요하다면 여기서 다시 Order 정보를 조회해서 상세 내용을 보여줄 수도 있음
        return "order/ordersuccess";
    }
}