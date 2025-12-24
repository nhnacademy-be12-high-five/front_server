package com.example.high_five.controller.admin;

import com.example.high_five.common.CommonPageResponse;
import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.order.OrderResponse;
import com.example.high_five.dto.order.OrderStatusUpdateRequest;
import com.example.high_five.service.OrderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // 1. 이 import 문을 꼭 추가하세요!

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderClient orderClient;

    @GetMapping
    @LoginRequired(adminOnly = true)
    public String getOrderPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model
    ) {
        CommonPageResponse<OrderResponse> response = orderClient.getAdminOrders(page, size, status);

        if (response != null && response.getData() != null) {
            model.addAttribute("orders", response.getData());
        } else {
            model.addAttribute("orders", Collections.emptyList());
        }

        model.addAttribute("pageInfo", response);
        model.addAttribute("selectedStatus", status);

        return "admin/orders";
    }

    @PutMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<Void> updateStatusApi(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        orderClient.updateOrderStatus(orderId, request);
        return ResponseEntity.ok().build();
    }
}