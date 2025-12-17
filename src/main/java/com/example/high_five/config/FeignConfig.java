package com.example.high_five.config;

import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
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
    public Retryer retryer() {
        return Retryer.NEVER_RETRY; // feign 호출 실패시 기본 5회 재시도에서 즉시 실패하게 바꿈
    }

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