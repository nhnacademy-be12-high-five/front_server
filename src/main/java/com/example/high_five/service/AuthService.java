package com.example.high_five.service;

import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.dto.member.response.TokenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "TEAM5-GATEWAY-SERVER", contextId = "authClient", url = "${gateway.uri}")
public interface AuthService {

    @PostMapping("/api/auth/login")
    ResponseEntity<TokenDto> login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/api/auth/logout")
    ResponseEntity<Void> logout();

    @PostMapping("/api/auth/reissue")
    ResponseEntity<TokenDto> reissue(@RequestHeader("X-Refresh-Token") String refreshToken);

    @PostMapping("/api/auth/login/{provider}")
    ResponseEntity<TokenDto> loginSocial(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code
    );

    @PostMapping("/api/auth/signup")
    ResponseEntity<MemberCreateRequestDto> signup(@RequestBody MemberCreateRequestDto memberRegisterRequestDto);

    // AJAX용
    @PostMapping("/api/auth/email/send")
    ResponseEntity<Void> sendEmail(@RequestParam("email") String email);

    @PostMapping("/api/auth/email/verify")
    ResponseEntity<String> verifyEmail(@RequestBody EmailVerifyRequest request);

    @GetMapping("/api/auth/exists/login-id/{loginId}")
    ResponseEntity<Boolean> checkLoginId(@PathVariable("loginId") String loginId);

}