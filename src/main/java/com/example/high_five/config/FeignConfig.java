package com.example.high_five.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Cookie[] cookies = request.getCookies();

                if (cookies != null) {
                    StringBuilder cookieHeader = new StringBuilder();

                    for (Cookie cookie : cookies) {

                        if ("access-token".equals(cookie.getName())) {
                            requestTemplate.header("Authorization", "Bearer " + cookie.getValue());
                        }


                        if (cookieHeader.length() > 0) {
                            cookieHeader.append("; ");
                        }
                        cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());
                    }


                    if (cookieHeader.length() > 0) {
                        requestTemplate.header("Cookie", cookieHeader.toString());
                    }
                }
            }
        };
    }
}