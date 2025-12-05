package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.request.LoginResponse;
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
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpServletResponse response,
                        Model model) {
        try {
            ResponseEntity<LoginResponse> apiResponse = authService.login(loginRequest);
            String accessToken = apiResponse.getBody().getAccessToken();

            ResponseCookie accessCookie = ResponseCookie.from("access-token", accessToken)
                    .path("/")
                    .httpOnly(true)
                    .secure(false) // 배포(HTTPS) 시 true
                    .sameSite("Strict")
                    .maxAge(accessExpirationTime)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

            List<String> cookies = apiResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null) {
                for (String cookieStr : cookies) {
                    if (cookieStr.contains("refresh-token")) {
                        String refreshTokenValue = cookieStr.split(";")[0].split("=")[1];

                        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", refreshTokenValue)
                                .path("/")
                                .httpOnly(true)
                                .secure(false) // 배포(HTTPS) 시 true
                                .sameSite("Strict")
                                .maxAge(refreshExpirationTime)
                                .build();

                        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                    }
                }
            }

            return "redirect:/";

        } catch (FeignException e) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
    }

    @GetMapping("/member/signup.html")
    public String signupForm() {
        return "member/signup";
    }

}