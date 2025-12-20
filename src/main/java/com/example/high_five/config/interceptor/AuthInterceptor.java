package com.example.high_five.config.interceptor;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.common.utils.JwtPayloadParser;
import com.example.high_five.dto.context.UserContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = getCookieValue(request, "access-token");

        if (token != null) {
            try {
                token = URLDecoder.decode(token, StandardCharsets.UTF_8);
                Map<String, Object> claims = JwtPayloadParser.parseClaims(token);

                if (!claims.isEmpty()) {
                   Object sub = claims.get("sub");
                    Long memberId = sub != null ? Long.valueOf(String.valueOf(sub)) : null;
                    
                    String role = (String) claims.get("role");

                    if (memberId != null) {
                        UserContext userContext = new UserContext(memberId, role);
                        request.setAttribute("user", userContext);
                    }
                }
            } catch (Exception e) {
                log.warn("토큰 파싱 중 오류 발생 (무시하고 진행): {}", e.getMessage());
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }


        if (loginRequired.adminOnly() && !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
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
}