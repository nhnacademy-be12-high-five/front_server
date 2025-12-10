package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration_time}")
    private Long accessExpirationTime;

    @Value("${jwt.refresh_expiration_time}")
    private Long refreshExpirationTime;

    private static final boolean IS_SECURE = true;

    @GetMapping("/member/login.html")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute LoginRequest loginRequest, HttpServletResponse response) {

        ResponseEntity<TokenDto> apiResponse = authService.login(loginRequest);
        TokenDto tokens = apiResponse.getBody();

        if (tokens == null) {
            throw new RuntimeException("로그인 실패: 토큰이 없습니다.");
        }
        setCookie(response, "access-token", tokens.getAccessToken(), accessExpirationTime);
        setCookie(response, "refresh-token", tokens.getRefreshToken(), refreshExpirationTime);

        return "redirect:/";
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveCookie(request, "refresh-token");

        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        ResponseEntity<TokenDto> apiResponse = authService.reissue(refreshToken);
        TokenDto newTokens = apiResponse.getBody();

        setCookie(response, "access-token", newTokens.getAccessToken(), accessExpirationTime);
        setCookie(response, "refresh-token", newTokens.getRefreshToken(), refreshExpirationTime);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletResponse response) {
        try {
            authService.logout(); // 파라미터 없이 호출
        } catch (Exception e) {
            log.warn("로그아웃 처리 중 오류 (무시): {}", e.getMessage());
        }

        setCookie(response, "access-token", "", 0);
        setCookie(response, "refresh-token", "", 0);

        return "redirect:/";
    }

    @GetMapping("/member/signup.html")
    public String signupForm() {
        return "member/signup";
    }

    // 쿠키 설정 중복 제거를 위한 헬퍼 메서드
    private void setCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(IS_SECURE) // ★ 모든 메서드에서 동일하게 적용
                .sameSite("Strict") // ★ 모든 메서드에서 동일하게 적용
                .maxAge(maxAgeSeconds / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}