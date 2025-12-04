package com.example.high_five.service;

import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.point.PointAdminAdjustmentRequest;
import com.example.high_five.dto.point.PointAdminPolicyRequest;
import com.example.high_five.dto.point.PointAdminPolicyResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.dto.point.PointHistoryResponse;
import com.example.high_five.dto.point.PointTransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name = "gateway-server", contextId = "memberClient", url = "http://gateway-server:8000")
@FeignClient(name = "gateway-server", contextId = "memberClient", url = "http://localhost:8082")
public interface MemberService {
    // 관리자 - 정책 조회
    @GetMapping("/api/admin/points/policy")
    ResponseEntity<PointAdminPolicyResponse> getPolicy();

    // 관리자 - 정책 수정
    @PostMapping("/api/admin/points/policy")
    ResponseEntity<Void> updatePolicy(@RequestBody PointAdminPolicyRequest request);

    // 관리자 - 회원 포인트 조정
    @PostMapping("/api/admin/points/adjustment")
    ResponseEntity<PointTransactionResponse> manualAdjustment(@RequestBody PointAdminAdjustmentRequest request);

    // 포인트 잔액 조회
    @GetMapping("/api/points/balance")
    ResponseEntity<PointBalanceResponse> getMyBalance(@RequestHeader("X-USER-ID") Long memberId);

    // 포인트 이력 조회
    @GetMapping("/api/points/history")
    ResponseEntity<CustomPage<PointHistoryResponse>> getMyHistory(
                                                                   @RequestHeader("X-USER-ID") Long memberId,
                                                                   @RequestParam("page") int page,
                                                                   @RequestParam("size") int size);
}
