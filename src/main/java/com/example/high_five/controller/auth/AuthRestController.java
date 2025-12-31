package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.DormantRequest;
import com.example.high_five.dto.member.request.EmailRequest;
import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위한 import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j // 1. 로그 기능을 자동으로 추가
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final MemberService memberService;

    // 1. 이메일 인증번호 발송
    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid EmailRequest email) {
        log.info("====== [AuthRestController] 이메일 인증 요청 진입 ======");
        log.info("요청 데이터: {}", email); // EmailRequest의 toString()이 찍힘

        try {
            log.info("[Feign 호출 시도] AuthService.sendEmail() 호출...");
            authService.sendEmail(email);
            log.info("[Feign 호출 성공] 인증번호 발송 완료");

            return ResponseEntity.ok("인증번호가 발송되었습니다.");

        } catch (FeignException e) {
            logErrorDetails(e, "sendEmail"); // 에러 상세 로그 출력 메서드 호출

            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    // 2. 이메일 인증번호 검증
    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid EmailVerifyRequest request) {
        log.info("====== [AuthRestController] 이메일 코드 검증 요청 진입 ======");
        log.info("요청 데이터: {}", request);

        try {
            log.info("[Feign 호출 시도] AuthService.verifyEmail() 호출...");
            ResponseEntity<String> response = authService.verifyEmail(request);
            log.info("[Feign 호출 성공] 검증 완료. 응답: {}", response.getBody());

            return response;

        } catch (FeignException e) {
            logErrorDetails(e, "verifyEmail");
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    // 3. 아이디 중복 확인
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam("loginId") String loginId) {
        log.info("====== [AuthRestController] 아이디 중복 확인 요청: {} ======", loginId);

        try {
            return authService.checkId(loginId);
        } catch (Exception e) {
            log.error("[AuthRestController] 아이디 중복 확인 중 알 수 없는 에러: ", e);
            return ResponseEntity.status(500).body(false);
        }
    }

    // 4. 아이디 찾기 인증번호 발송
    @PostMapping("/email/find-id")
    public ResponseEntity<String> sendFindIdCode(@RequestBody @Valid EmailRequest email) {
        log.info("====== [AuthRestController] 아이디 찾기 코드 발송 요청 ======");
        log.info("이메일: {}", email.getEmail());

        try {
            authService.sendFindIdCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            logErrorDetails(e, "sendFindIdCode");
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    // 5. 비밀번호 재설정 인증번호 발송
    @PostMapping("/email/password-reset")
    public ResponseEntity<String> sendPasswordResetCode(@RequestBody @Valid EmailRequest email) {
        log.info("====== [AuthRestController] 비밀번호 재설정 코드 발송 요청 ======");

        try {
            authService.sendPasswordResetCode(email);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (FeignException e) {
            logErrorDetails(e, "sendPasswordResetCode");
            String msg = extractFeignMessage(e);
            return ResponseEntity.status(e.status()).body(msg);
        }
    }

    // 6. 휴면 해제 인증번호 발송
    @PostMapping("/dormant/send")
    public ResponseEntity<String> sendDormantCode(@RequestBody @Valid DormantRequest request) {
        log.info("====== [AuthRestController] 휴면 해제 인증번호 발송 요청 ======");

        try {
            memberService.checkDormantMember(request);
            EmailRequest emailReq = new EmailRequest(request.getEmail());
            memberService.sendDormantEmail(emailReq);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");

        } catch (FeignException e) {
            logErrorDetails(e, "sendDormantCode");
            return ResponseEntity.status(e.status()).body("정보가 일치하지 않습니다.");
        }
    }

    // 7. 휴면 해제 검증
    @PostMapping("/dormant/verify")
    public ResponseEntity<String> verifyAndActivate(@RequestBody @Valid DormantRequest request) {
        log.info("====== [AuthRestController] 휴면 해제 최종 검증 요청 ======");

        try {
            memberService.activateDormant(request);
            return ResponseEntity.ok("휴면 상태가 해제되었습니다.");
        } catch (Exception e) {
            log.error("[AuthRestController] 휴면 해제 중 에러 발생", e);
            return ResponseEntity.badRequest().body("인증번호가 틀렸거나 오류가 발생했습니다.");
        }
    }

    // === Private Helper Methods ===

    // Feign 에러 발생 시 상세 정보를 로그로 남기는 메서드
    private void logErrorDetails(FeignException e, String methodName) {
        log.error("🛑 [Feign Error] 메서드: {}", methodName);
        log.error("   - Status Code: {}", e.status());
        log.error("   - Request URL: {}", e.request().url()); // ★ 여기가 가장 중요합니다 (404 원인 파악)
        log.error("   - Request Method: {}", e.request().httpMethod());
        log.error("   - Response Body: {}", e.contentUTF8());
    }

    private String extractFeignMessage(FeignException e) {
        try {
            String body = e.contentUTF8();
            if (body == null || body.isBlank()) return "요청 처리 중 오류가 발생했습니다.";

            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) return node.get("message").asText();

            return body;
        } catch (Exception ex) {
            log.warn("Feign 에러 메시지 파싱 실패: {}", ex.getMessage());
            return "요청 처리 중 오류가 발생했습니다.";
        }
    }
}