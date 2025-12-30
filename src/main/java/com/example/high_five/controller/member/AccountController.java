package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.dto.member.request.PasswordResetRequest;
import com.example.high_five.service.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<String> findId(@RequestBody @Valid EmailVerifyRequest request) {
        try {
            return authService.findId(request);
        } catch (FeignException e) {
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    @PostMapping("/api/find/password")
    @ResponseBody
    public ResponseEntity<String> resetPassword(@RequestBody @Valid PasswordResetRequest request,
                                                BindingResult bindingResult) { // ★ 1. 여기 추가!

        // ★ 2. 유효성 검사 실패 시, 첫 번째 에러 메시지를 바로 반환
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        try {
            authService.resetPassword(request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");

        } catch (FeignException e) {
            // Feign 에러 파싱 (기존 로직)
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("시스템 오류");
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