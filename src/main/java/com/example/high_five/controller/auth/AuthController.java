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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/member/login")
    public String loginForm() {
        return "member/login";
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute LoginRequest loginRequest,
                        HttpServletResponse response,
                        RedirectAttributes rttr) {
        try {
            ResponseEntity<TokenDto> apiResponse = authService.login(loginRequest);
            TokenDto tokens = apiResponse.getBody();

            if (tokens == null) {
                throw new RuntimeException("토큰 없음");
            }

            setCookie(response, "access-token", tokens.getAccessToken(), accessExpirationTime);
            setCookie(response, "refresh-token", tokens.getRefreshToken(), refreshExpirationTime);

            return "redirect:/";

        } catch (Exception e) {
            log.warn("로그인 실패: {}", e.getMessage());
            rttr.addFlashAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "redirect:/member/login";
        }
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveCookie(request, "refresh-token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            ResponseEntity<TokenDto> apiResponse = authService.reissue(refreshToken);
            TokenDto newTokens = apiResponse.getBody();

            if(newTokens != null) {
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
    public String socialLoginCallback(@PathVariable String provider, @RequestParam("code") String code,
                                      HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            ResponseEntity<TokenDto> apiResponse = authService.loginSocial(provider, code);
            TokenDto tokens = apiResponse.getBody();

            if (tokens == null) throw new RuntimeException("소셜 로그인 실패");

            setCookie(response, "access-token", tokens.getAccessToken(), accessExpirationTime);
            setCookie(response, "refresh-token", tokens.getRefreshToken(), refreshExpirationTime);

            if (!tokens.isProfileComplete()) {
                redirectAttributes.addFlashAttribute("alertMessage", "필수 정보를 입력해주세요.");
                return "redirect:/mypage?tab=info";
            }
            return "redirect:/";

        } catch (Exception e) {
            log.error("소셜 로그인 실패: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "소셜 로그인 중 오류가 발생했습니다.");
            return "redirect:/member/login";
        }
    }

    private void setCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/").httpOnly(true).secure(IS_SECURE).sameSite("Lax")
                .maxAge(maxAgeSeconds / 1000).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }
}