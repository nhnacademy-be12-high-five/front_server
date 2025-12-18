package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/api")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestParam("email") String email) {
        try {
            authService.sendEmail(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            String msg = e.contentUTF8();
            return ResponseEntity.status(e.status()).body(msg != null ? msg : "발송 실패");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("오류: " + e.getMessage());
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerifyRequest request) {
        try {
            return authService.verifyEmail(request);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("인증 실패");
        }
    }

    @PostMapping("/id/check")
    public ResponseEntity<String> checkId(@RequestParam("loginId") String loginId) {
        if (loginId == null || loginId.isBlank()) return ResponseEntity.badRequest().body("아이디 입력 필요");
        
        try {
            Boolean exists = authService.checkLoginId(loginId).getBody();
            if (Boolean.TRUE.equals(exists)) {
                return ResponseEntity.status(409).body("이미 사용 중인 아이디입니다.");
            }
            return ResponseEntity.ok("사용 가능한 아이디입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("확인 중 오류 발생");
        }
    }
}