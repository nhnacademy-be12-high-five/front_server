package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.EmailRequest;
import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest email) {
        try {
            authService.sendEmail(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body("발송 실패: " + e.contentUTF8());
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerifyRequest request) {
        try {
            return authService.verifyEmail(request);
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body("인증 실패");
        }
    }

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam("loginId") String loginId) {
        try {
            return authService.checkId(loginId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false); // 에러나면 중복된 걸로 처리(가입 막기)
        }
    }

    @PostMapping("/email/find-id")
    public ResponseEntity<String> sendFindIdCode(@RequestBody EmailRequest email) {
        try {
            authService.sendFindIdCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body("가입되지 않은 이메일입니다.");
        }
    }

    @PostMapping("/email/password-reset")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody EmailRequest email) {
        try {
            authService.sendPasswordResetCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body("가입 정보를 찾을 수 없습니다.");
        }
    }
}