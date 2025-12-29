package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.exception.FeignErrorParser;
import com.example.high_five.exception.LoginErrorMapper;
import com.example.high_five.service.AuthService;
import feign.FeignException;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final FeignErrorParser feignErrorParser;

    @Value("${jwt.expiration_time}")
    private Long accessExpirationTime;

    @Value("${jwt.refresh_expiration_time}")
    private Long refreshExpirationTime;

    private static final boolean IS_SECURE = true;

    @GetMapping("/member/login")
    public String loginForm(
            @RequestParam(value = "errorCode", required = false) String errorCode,
            Model model
    ) {
        if (errorCode != null && !errorCode.isBlank()) {
            model.addAttribute("errorMessage", LoginErrorMapper.toMessage(errorCode));
        }
        return "member/login";
    }

    @PostMapping("/auth/login")
    public String login(
            @ModelAttribute LoginRequest loginRequest,
            HttpServletResponse response,
            RedirectAttributes rttr
    ) {
        try {
            ResponseEntity<TokenDto> apiResponse = authService.login(loginRequest);
            TokenDto tokens = apiResponse.getBody();

            if (tokens == null) throw new RuntimeException("토큰 없음");

            setCookie(response, "access-token", tokens.getAccessToken(), accessExpirationTime);
            setCookie(response, "refresh-token", tokens.getRefreshToken(), refreshExpirationTime);

            return "redirect:/";

        } catch (FeignException e) {
            FeignErrorParser.FeignError fe =
                    feignErrorParser.parse(e, "C002", "로그인에 실패했습니다.");

            log.warn("로그인 실패 (Feign): code={}, message={}", fe.code(), fe.message());
            rttr.addAttribute("errorCode", fe.code());
            return "redirect:/member/login";

        } catch (Exception e) {
            log.error("로그인 시스템 오류", e);
            rttr.addAttribute("errorCode", "C002");
            return "redirect:/member/login";
        }
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveCookie(request, "refresh-token");
        if (refreshToken == null) return ResponseEntity.status(401).build();

        try {
            ResponseEntity<TokenDto> apiResponse = authService.reissue(refreshToken);
            TokenDto newTokens = apiResponse.getBody();

            if (newTokens != null) {
                setCookie(response, "access-token", newTokens.getAccessToken(), accessExpirationTime);
                setCookie(response, "refresh-token", newTokens.getRefreshToken(), refreshExpirationTime);
            }
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/logout")
    public String logoutByGet(HttpServletResponse response) {
        return processLogout(response);
    }

    @PostMapping("/auth/logout")
    public String logout(HttpServletResponse response) {
        return processLogout(response);
    }

    private String processLogout(HttpServletResponse response) {
        try {
            authService.logout();
        } catch (Exception e) {
            log.warn("로그아웃 오류 (무시): {}", e.getMessage());
        }
        setCookie(response, "access-token", "", 0);
        setCookie(response, "refresh-token", "", 0);
        return "redirect:/";
    }

    @GetMapping("/login/oauth2/code/{provider}")
    public String socialLoginCallback(
            @PathVariable String provider,
            @RequestParam("code") String code,
            HttpServletResponse response,
            RedirectAttributes rttr
    ) {
        try {
            ResponseEntity<TokenDto> apiResponse = authService.loginSocial(provider, code);
            TokenDto tokens = apiResponse.getBody();

            if (tokens == null) throw new RuntimeException("소셜 로그인 실패");

            setCookie(response, "access-token", tokens.getAccessToken(), accessExpirationTime);
            setCookie(response, "refresh-token", tokens.getRefreshToken(), refreshExpirationTime);

            if (!tokens.isProfileComplete()) {
                rttr.addFlashAttribute("alertMessage", "필수 정보를 입력해주세요.");
                return "redirect:/mypage?tab=info";
            }

            return "redirect:/";

        } catch (FeignException e) {
            FeignErrorParser.FeignError fe =
                    feignErrorParser.parse(e, "C002", "소셜 로그인에 실패했습니다.");

            log.warn("소셜 로그인 실패 (Feign): code={}, message={}", fe.code(), fe.message());
            rttr.addAttribute("errorCode", fe.code());
            return "redirect:/member/login";

        } catch (Exception e) {
            log.error("소셜 로그인 실패", e);
            rttr.addAttribute("errorCode", "C002");
            return "redirect:/member/login";
        }
    }

    private void setCookie(HttpServletResponse response, String name, String value, long maxAgeMillis) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(IS_SECURE)
                .sameSite("Lax")
                .maxAge(maxAgeMillis / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}