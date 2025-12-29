package com.example.high_five.common;

import com.example.high_five.common.utils.JwtPayloadParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberIdResolverTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private MemberIdResolver memberIdResolver;

    @Test
    @DisplayName("쿠키가 없으면 예외 발생")
    void resolveRequired_noCookie() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> memberIdResolver.resolveRequired())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그인이 필요합니다.");
    }

    @Test
    @DisplayName("토큰에 memberId가 있으면 ID 반환")
    void resolveRequired_success() {
        // given
        Cookie cookie = new Cookie("access-token", "dummy-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        try (MockedStatic<JwtPayloadParser> parser = mockStatic(JwtPayloadParser.class)) {
            parser.when(() -> JwtPayloadParser.parseClaims(anyString()))
                    .thenReturn(Map.of("memberId", 123L));

            // when
            Long memberId = memberIdResolver.resolveRequired();

            // then
            assertThat(memberId).isEqualTo(123L);
        }
    }

    @Test
    @DisplayName("다양한 key(userId, id 등) 지원 확인")
    void resolveRequired_alternativeKeys() {
        // given
        Cookie cookie = new Cookie("access-token", "dummy-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        try (MockedStatic<JwtPayloadParser> parser = mockStatic(JwtPayloadParser.class)) {
            // memberId 대신 userId가 들어있는 경우
            parser.when(() -> JwtPayloadParser.parseClaims(anyString()))
                    .thenReturn(Map.of("userId", 456));

            // when
            Long memberId = memberIdResolver.resolveRequired();

            // then
            assertThat(memberId).isEqualTo(456L);
        }
    }
}