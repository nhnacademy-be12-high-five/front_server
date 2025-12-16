package com.example.high_five.service;

import com.example.high_five.dto.payment.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "payment-server", url = "${gateway.uri}")
public interface PaymentService {

    @GetMapping("/api/payments/admin/stats/summary")
    PaymentStatsResponse getTotalStats();

    @GetMapping("/api/payments/admin/stats/daily")
    List<DailySalesResponse> getDailyStats(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    @PostMapping("/api/payments/confirm")
    PaymentConfirmResponse confirmPayment(@RequestBody PaymentConfirmRequest request);

    // 활성화된 결제 수단 조회
    @GetMapping("/api/payments/methods")
    List<PaymentMethodResponse> getAllMethods();

    // 관리자용 상태 변경 API
    @PutMapping("/api/payments/methods/admin/{methodId}/status")
    void updateStatus(@PathVariable("methodId") Long methodId, @RequestBody MethodStatusRequest request);
}