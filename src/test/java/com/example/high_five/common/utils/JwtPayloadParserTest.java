package com.example.high_five.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPayloadParserTest {

    @Test
    @DisplayName("정상적인 토큰 파싱 성공")
    void parseClaims_success() {
        // given
        // {"sub":"12345", "role":"USER", "exp":1735435000} 의 Base64 인코딩
        String payloadJson = "{\"sub\":\"12345\", \"role\":\"USER\", \"exp\":1735435000}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String token = "header." + encodedPayload + ".signature";

        // when
        Map<String, Object> claims = JwtPayloadParser.parseClaims(token);

        // then
        assertThat(claims).isNotEmpty();
        assertThat(claims.get("sub")).isEqualTo("12345");
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(claims.get("exp")).isEqualTo(1735435000);
    }

    @Test
    @DisplayName("Bearer 접두사가 있어도 파싱 성공")
    void parseClaims_withBearerPrefix() {
        // given
        String payloadJson = "{\"id\":1}";
        String encodedPayload = Base64.getUrlEncoder().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String token = "Bearer header." + encodedPayload + ".signature";

        // when
        Map<String, Object> claims = JwtPayloadParser.parseClaims(token);

        // then
        assertThat(claims).containsEntry("id", 1);
    }

    @Test
    @DisplayName("토큰 형식이 잘못된 경우 빈 맵 반환")
    void parseClaims_invalidFormat() {
        // when & then
        assertThat(JwtPayloadParser.parseClaims(null)).isEmpty();
        assertThat(JwtPayloadParser.parseClaims("")).isEmpty();
        assertThat(JwtPayloadParser.parseClaims("invalid.token")).isEmpty(); // 점이 2개가 아님
    }

    @Test
    @DisplayName("Payload가 유효한 JSON이 아닌 경우 빈 맵 반환")
    void parseClaims_invalidJson() {
        // given
        String invalidPayload = Base64.getUrlEncoder().encodeToString("{not-json}".getBytes());
        String token = "head." + invalidPayload + ".sign";

        // when
        Map<String, Object> claims = JwtPayloadParser.parseClaims(token);

        // then
        assertThat(claims).isEmpty();
    }
}