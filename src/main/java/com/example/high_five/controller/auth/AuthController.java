package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.request.LoginResponse;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.service.AuthService;
import feign.FeignException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration_time}")
    private Long accessExpirationTime;

    @Value("${jwt.refresh_expiration_time}")
    private Long refreshExpirationTime;

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
        ResponseCookie accessCookie = ResponseCookie.from("access-token", tokens.getAccessToken())
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS 적용 시 true로 변경
                .maxAge(accessExpirationTime)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", tokens.getRefreshToken())
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS 적용 시 true로 변경
                .maxAge(refreshExpirationTime)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return "redirect:/";
    }

    @GetMapping("/member/signup.html")
    public String signupForm() {
        return "member/signup";
    }

}