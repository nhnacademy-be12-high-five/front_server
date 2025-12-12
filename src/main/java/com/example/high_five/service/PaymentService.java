package com.example.high_five.service;

import com.example.high_five.dto.payment.MethodStatusRequest;
import com.example.high_five.dto.payment.PaymentConfirmRequest;
import com.example.high_five.dto.payment.PaymentConfirmResponse;
import com.example.high_five.dto.payment.PaymentMethodResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-server", contextId = "paymentClient", url = "${gateway.uri}")
public interface PaymentService {

    @PostMapping("/api/payments/confirm")
    PaymentConfirmResponse confirmPayment(@RequestBody PaymentConfirmRequest request);

    // 활성화된 결제 수단 조회
    @GetMapping("/api/payments/methods")
    List<PaymentMethodResponse> getAllMethods();

    // 관리자용 상태 변경 API
    @PutMapping("/api/payments/methods/admin/{methodId}/status")
    void updateStatus(@PathVariable("methodId") Long methodId, @RequestBody MethodStatusRequest request);
}