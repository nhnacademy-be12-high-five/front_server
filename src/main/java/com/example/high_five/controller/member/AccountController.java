package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.EmailRequest;
import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.dto.member.request.PasswordResetRequest;
import com.example.high_five.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AuthService authService;

    @GetMapping("/find/id")
    public String findIdForm() {
        return "member/find-id";
    }

    @GetMapping("/find/password")
    public String findPasswordForm() {
        return "member/find-password";
    }

    @PostMapping("/api/find/id")
    @ResponseBody
    public ResponseEntity<String> findId(@RequestBody EmailVerifyRequest request) {
        try {
            return authService.findId(request);
        } catch (FeignException e) {
            String backendMessage = e.contentUTF8();
            if (backendMessage == null || backendMessage.isBlank()) {
                backendMessage = "인증에 실패했습니다.";
            }
            return ResponseEntity.status(e.status()).body(backendMessage);
        }
    }

    @PostMapping("/api/find/password")
    @ResponseBody
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        return authService.resetPassword(request);
    }
}