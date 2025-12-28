package com.example.high_five.config.interceptor;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.common.utils.JwtPayloadParser;
import com.example.high_five.dto.context.UserContext;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Value("${jwt.expiration_time}")
    private Long accessExpirationTime;

    @Value("${jwt.refresh_expiration_time}")
    private Long refreshExpirationTime;

    public AuthInterceptor(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = getCookieValue(request, "access-token");
        String refreshToken = getCookieValue(request, "refresh-token");

        if (token != null) {
            try {
                String decodedToken = URLDecoder.decode(token, StandardCharsets.UTF_8);
                Map<String, Object> claims = JwtPayloadParser.parseClaims(decodedToken);

                if (!claims.isEmpty()) {
                    Object sub = claims.get("sub");
                    Long memberId = sub != null ? Long.valueOf(String.valueOf(sub)) : null;
                    String role = (String) claims.get("role");

                    if (memberId != null) {
                        UserContext userContext = new UserContext(memberId, role);
                        request.setAttribute("user", userContext);
                    }

                    Object expObj = claims.get("exp");
                    if (expObj != null) {
                        long expTime = ((Number) expObj).longValue();
                        long currentTime = System.currentTimeMillis() / 1000;
                        long timeRemaining = expTime - currentTime;

                        if (timeRemaining < 300 && refreshToken != null) {
                            log.info("Access Token expiry nearby ({}s remaining). Attempting auto-reissue.", timeRemaining);

                            try {
                                TokenDto newTokens = authService.reissue(refreshToken).getBody();

                                if (newTokens != null) {
                                    setCookie(response, "access-token", newTokens.getAccessToken(), accessExpirationTime);
                                    setCookie(response, "refresh-token", newTokens.getRefreshToken(), refreshExpirationTime);
                                    log.info("Token successfully reissued and cookies updated.");
                                }
                            } catch (Exception e) {
                                log.warn("Auto-reissue failed: {}", e.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing token in interceptor: {}", e.getMessage());
            }
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);

        if (loginRequired == null) {
            return true;
        }

        UserContext user = (UserContext) request.getAttribute("user");

        if (user == null) {
            String requestURI = request.getRequestURI();
            response.sendRedirect("/member/login?needLogin=true&redirectURL=" + requestURI);
            return false;
        }

        if (loginRequired.adminOnly() && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin permission required.");
            return false;
        }

        return true;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void setCookie(HttpServletResponse response, String name, String value, long maxAgeMillis) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(maxAgeMillis / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}