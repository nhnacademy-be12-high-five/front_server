package com.example.high_five.service;

import com.example.high_five.dto.payment.DailySalesResponse;
import com.example.high_five.dto.payment.PaymentStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}