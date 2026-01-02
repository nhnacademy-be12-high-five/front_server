package com.example.high_five.service;

import com.example.high_five.dto.member.request.*;
import com.example.high_five.dto.member.response.TokenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-server", contextId = "authClient", url = "${gateway.uri}")
public interface AuthService {

    @PostMapping("/api/auth/login")
    ResponseEntity<TokenDto> login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/api/auth/logout")
    ResponseEntity<Void> logout();

    @PostMapping("/api/auth/reissue")
    ResponseEntity<TokenDto> reissue(@RequestHeader("X-Refresh-Token") String refreshToken);

    @PostMapping("/api/auth/login/{provider}")
    ResponseEntity<TokenDto> loginSocial(@PathVariable("provider") String provider, @RequestParam("code") String code);

    @PostMapping("/api/accounts/signup")
    ResponseEntity<Void> signup(@RequestBody MemberCreateRequest request);

    @GetMapping("/api/accounts/check-id")
    ResponseEntity<Boolean> checkId(@RequestParam("loginId") String loginId);

    @PostMapping("/api/accounts/find/id/verify")
    ResponseEntity<String> findId(@RequestBody EmailVerifyRequest request);

    @PostMapping("/api/accounts/find/password")
    ResponseEntity<Void> resetPassword(@RequestBody PasswordResetRequest request);

    @PostMapping("/api/emails/find-id")
    ResponseEntity<Void> sendFindIdCode(@RequestBody EmailRequest email);

    @PostMapping("/api/emails/password-reset")
    ResponseEntity<Void> sendPasswordResetCode(@RequestBody EmailRequest email);

    @PostMapping("/api/emails/signup")
    ResponseEntity<Void> sendEmail(@RequestBody EmailRequest email);

    @PostMapping("/api/emails/verify")
    ResponseEntity<String> verifyEmail(@RequestBody EmailVerifyRequest request);
}