package com.example.high_five.service;

import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.dto.point.PointAdminAdjustmentRequest;
import com.example.high_five.dto.point.PointAdminPolicyRequest;
import com.example.high_five.dto.point.PointAdminPolicyResponse;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.dto.point.PointHistoryResponse;
import com.example.high_five.dto.point.PointTransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-server", contextId = "memberClient", url = "${gateway.uri}")
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
    ResponseEntity<PointBalanceResponse> getMyBalance(@RequestHeader("Authorization") String token);

    // 포인트 이력 조회
    @GetMapping("/api/points/history")
    ResponseEntity<CustomPage<PointHistoryResponse>> getMyHistory(@RequestHeader("Authorization") String token,
                                                                  @RequestParam("page") int page,@RequestParam("size") int size);

    @GetMapping("/api/members/me")
    ResponseEntity<MemberResponse> getMyInfo();

    @PutMapping("/api/members/me")
    ResponseEntity<MemberResponse> updateMember(@RequestBody MemberUpdateRequest request);

    @DeleteMapping("/api/members/me/withdraw")
    ResponseEntity<Void> withdrawMember();
}
