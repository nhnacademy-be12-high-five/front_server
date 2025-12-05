package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.request.LoginResponse;
import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.service.AuthService;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
            Cookie accessCookie = new Cookie("access-token", accessToken);
            accessCookie.setPath("/");
            accessCookie.setHttpOnly(true);
            response.addCookie(accessCookie);

            List<String> cookies = apiResponse.getHeaders().get("Set-Cookie");
            if (cookies != null) {
                for (String cookieStr : cookies) {
                    if (cookieStr.contains("refresh-token")) {
                        String refreshTokenValue = cookieStr.split(";")[0].split("=")[1];
                        Cookie refreshCookie = new Cookie("refresh-token", refreshTokenValue);
                        refreshCookie.setPath("/");
                        refreshCookie.setHttpOnly(true);
                        refreshCookie.setMaxAge(86400);
                        response.addCookie(refreshCookie);
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