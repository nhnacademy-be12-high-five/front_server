package com.example.high_five.config.interceptor;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.common.utils.JwtPayloadParser;
import com.example.high_five.dto.context.UserContext;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock private AuthService authService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HandlerMethod handlerMethod;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        // @Value 필드 주입 시뮬레이션
        ReflectionTestUtils.setField(authInterceptor, "accessExpirationTime", 3600000L);
        ReflectionTestUtils.setField(authInterceptor, "refreshExpirationTime", 1209600000L);
    }

    @Test
    @DisplayName("토큰이 유효하면 UserContext를 request에 설정")
    void preHandle_validToken() throws Exception {
        // given
        Cookie accessCookie = new Cookie("access-token", "valid-token");
        given(request.getCookies()).willReturn(new Cookie[]{accessCookie});

        // 정적 메서드 모킹
        try (MockedStatic<JwtPayloadParser> parser = mockStatic(JwtPayloadParser.class)) {
            parser.when(() -> JwtPayloadParser.parseClaims(anyString()))
                    .thenReturn(Map.of("sub", 1L, "role", "USER", "exp", System.currentTimeMillis() / 1000 + 3600));

            // @LoginRequired 없는 핸들러라고 가정
            given(handlerMethod.getMethodAnnotation(LoginRequired.class)).willReturn(null);

            // when
            boolean result = authInterceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(result).isTrue();
            verify(request).setAttribute(eq("user"), any(UserContext.class));
        }
    }

    @Test
    @DisplayName("LoginRequired가 있지만 유저 정보가 없으면 로그인 페이지로 리다이렉트")
    void preHandle_loginRequired_fail() throws Exception {
        // given
        given(request.getCookies()).willReturn(null); // 쿠키 없음

        LoginRequired loginRequired = mock(LoginRequired.class);
        given(handlerMethod.getMethodAnnotation(LoginRequired.class)).willReturn(loginRequired);
        given(request.getRequestURI()).willReturn("/target-url");

        // when
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // then
        assertThat(result).isFalse();
        verify(response).sendRedirect(contains("/member/login"));
    }

    @Test
    @DisplayName("토큰 만료가 임박하면(300초 미만) 재발급 시도")
    void preHandle_autoReissue() throws Exception {
        // given
        Cookie access = new Cookie("access-token", "expiring-token");
        Cookie refresh = new Cookie("refresh-token", "refresh-token-val");
        given(request.getCookies()).willReturn(new Cookie[]{access, refresh});

        try (MockedStatic<JwtPayloadParser> parser = mockStatic(JwtPayloadParser.class)) {
            // 만료시간이 100초 남았다고 설정
            long exp = System.currentTimeMillis() / 1000 + 100;
            parser.when(() -> JwtPayloadParser.parseClaims(anyString()))
                    .thenReturn(Map.of("sub", 1L, "exp", exp));

          //  TokenDto newTokens = new TokenDto("new-access", "new-refresh", "Bearer");
          //  given(authService.reissue(anyString())).willReturn(ResponseEntity.ok(newTokens));

            // when
            authInterceptor.preHandle(request, response, handlerMethod); // HandlerMethod 관련 로직은 무시(null 리턴 등으로 처리하거나 위에서 걸림)

            // then
            verify(authService).reissue("refresh-token-val");
            // Set-Cookie 헤더가 추가되었는지 확인
            verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"), contains("new-access"));
        }
    }
}