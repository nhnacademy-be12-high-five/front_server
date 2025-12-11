package com.example.high_five.service;

import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-server", contextId = "paymentClient", url = "${gateway.uri}")
public interface PaymentService {

    @PostMapping("/api/payments/confirm")
    PaymentConfirmResponse confirmPayment(@RequestBody PaymentConfirmRequest request);
}