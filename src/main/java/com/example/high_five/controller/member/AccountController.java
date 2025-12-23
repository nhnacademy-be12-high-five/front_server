package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.dto.member.request.PasswordResetRequest;
import com.example.high_five.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    @PostMapping("/api/find/password")
    @ResponseBody
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (FeignException e) {
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    private String extractFeignMessage(FeignException e) {
        try {
            String body = e.contentUTF8();
            if (body == null || body.isBlank()) return "요청 처리 중 오류가 발생했습니다.";

            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();

            return body;
        } catch (Exception ex) {
            return "요청 처리 중 오류가 발생했습니다.";
        }
    }
}