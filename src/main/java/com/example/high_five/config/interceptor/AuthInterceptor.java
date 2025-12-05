package com.example.high_five.config.interceptor;

import com.example.high_five.common.annotation.LoginRequired;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;


        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);


        if (loginRequired == null) {
            return true;
        }


        boolean hasToken = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access-token".equals(cookie.getName())) {
                    hasToken = true;
                    break;
                }
            }
        }

        if (!hasToken) {
            response.sendRedirect("/member/login.html");
            return false;
        }


        return true;
    }
}