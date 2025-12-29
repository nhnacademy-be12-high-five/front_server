package com.example.high_five.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FeignConfig.class)
class FeignConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Retryer 빈이 등록되어야 하며, 설정값은 NEVER_RETRY여야 함")
    void retryer_beanExists() {
        Retryer retryer = applicationContext.getBean(Retryer.class);

        assertThat(retryer).isNotNull();
        // 코드에서 return Retryer.NEVER_RETRY; 라고 했으므로 값 비교
        assertThat(retryer).isEqualTo(Retryer.NEVER_RETRY);
    }

    @Test
    @DisplayName("RequestInterceptor 빈이 등록되어야 함")
    void requestInterceptor_beanExists() {
        RequestInterceptor interceptor = applicationContext.getBean(RequestInterceptor.class);
        assertThat(interceptor).isNotNull();
    }

    @Test
    @DisplayName("RequestInterceptor 로직 검증: 쿠키가 헤더로 잘 복사되는지")
    void requestInterceptor_logic_copyCookies() {
        // given
        RequestInterceptor interceptor = applicationContext.getBean(RequestInterceptor.class);
        RequestTemplate template = new RequestTemplate();

        // 가짜 요청 컨텍스트 설정 (MockHttpServletRequest 사용)
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("access-token", "test-token-value"),
                new Cookie("other-cookie", "other-value"));
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        try {
            // when
            interceptor.apply(template);

            // then
            Map<String, Collection<String>> headers = template.headers();

            // 1. Authorization 헤더가 생성되었는지 확인
            assertThat(headers).containsKey("Authorization");
            assertThat(headers.get("Authorization")).contains("Bearer test-token-value");

            // 2. Cookie 헤더가 모든 쿠키를 포함하는지 확인
            assertThat(headers).containsKey("Cookie");
            String cookieHeader = headers.get("Cookie").iterator().next();
            assertThat(cookieHeader).contains("access-token=test-token-value");
            assertThat(cookieHeader).contains("other-cookie=other-value");

        } finally {
            // 테스트 후 컨텍스트 초기화 (다른 테스트에 영향 안 주도록)
            RequestContextHolder.resetRequestAttributes();
        }
    }
}