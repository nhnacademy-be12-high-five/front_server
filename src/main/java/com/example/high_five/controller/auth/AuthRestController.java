package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.DormantRequest;
import com.example.high_five.dto.member.request.EmailRequest;
import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest email) {
        try {
            authService.sendEmail(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody EmailVerifyRequest request) {
        try {
            return authService.verifyEmail(request);
        } catch (FeignException e) {
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam("loginId") String loginId) {
        try {
            return authService.checkId(loginId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("/email/find-id")
    public ResponseEntity<String> sendFindIdCode(@RequestBody EmailRequest email) {
        try {
            authService.sendFindIdCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    @PostMapping("/email/password-reset")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody EmailRequest email) {
        try {
            authService.sendPasswordResetCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
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

    @PostMapping("/dormant/send")
    public ResponseEntity<String> sendDormantCode(@RequestBody DormantRequest request) {
        try {
            memberService.checkDormantMember(request);

            EmailRequest emailReq = new EmailRequest(request.getEmail());
            memberService.sendDormantEmail(emailReq);

            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            return ResponseEntity.status(e.status()).body("정보가 일치하지 않습니다.");
        }
    }

    @PostMapping("/dormant/verify")
    public ResponseEntity<String> verifyAndActivate(@RequestBody DormantRequest request) {
        try {
            EmailVerifyRequest verifyReq = new EmailVerifyRequest(
                    request.getEmail(),
                    request.getAuthCode(),
                    "ACTIVATE"
            );
            authService.verifyEmail(verifyReq);

            memberService.activateDormant(request);

            return ResponseEntity.ok("휴면 상태가 해제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 오류가 발생했습니다.");
        }
    }
}