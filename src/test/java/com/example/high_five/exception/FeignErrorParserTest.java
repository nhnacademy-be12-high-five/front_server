package com.example.high_five.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FeignErrorParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FeignErrorParser parser = new FeignErrorParser(objectMapper);

    // 테스트용 Request 생성 헬퍼
    private Request createRequest() {
        return Request.create(HttpMethod.GET, "/api/test", Map.of(), null, null, null);
    }

    @Test
    @DisplayName("정상 JSON 파싱 - code와 message가 모두 있는 경우")
    void parse_ValidJson() {
        // given
        String json = "{\"code\": \"TEST_CODE\", \"message\": \"테스트 메시지\"}";
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                json.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("TEST_CODE");
        assertThat(error.message()).isEqualTo("테스트 메시지");
    }

    @Test
    @DisplayName("JSON 파싱 성공 - code 필드가 누락된 경우 기본값(defaultCode) 사용")
    void parse_MissingCode() {
        // given
        String json = "{\"message\": \"테스트 메시지\"}"; // code 없음
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                json.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("DEFAULT_CODE");
        assertThat(error.message()).isEqualTo("테스트 메시지");
    }

    @Test
    @DisplayName("JSON 파싱 성공 - message 필드가 누락된 경우 기본값(defaultMessage) 사용")
    void parse_MissingMessage() {
        // given
        String json = "{\"code\": \"TEST_CODE\"}"; // message 없음
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                json.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("TEST_CODE");
        assertThat(error.message()).isEqualTo("Default Message");
    }

    @Test
    @DisplayName("유효하지 않은 JSON 형식이면 파싱 예외 처리 후 Fallback 로직 수행")
    void parse_InvalidJson() {
        // given
        String invalidJson = "{ invalid json body }";
        // 400 Bad Request
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                invalidJson.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        // 파싱 실패 -> statusFallback 실행 -> 400이므로 default 반환
        assertThat(error.code()).isEqualTo("DEFAULT_CODE");
        assertThat(error.message()).isEqualTo("Default Message");
    }

    @Test
    @DisplayName("Body가 없는(null) 경우 Fallback 로직 수행")
    void parse_NullBody() {
        // given
        FeignException exception = new FeignException.InternalServerError(
                "Error",
                createRequest(),
                null, // Body null
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("DEFAULT_CODE");
        assertThat(error.message()).isEqualTo("Default Message");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Body가 빈 문자열(Blank)인 경우 Fallback 로직 수행")
    void parse_BlankBody(String body) {
        // given
        FeignException exception = new FeignException.BadRequest(
                "Error",
                createRequest(),
                body.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("DEFAULT_CODE");
        assertThat(error.message()).isEqualTo("Default Message");
    }

    @Test
    @DisplayName("Fallback 로직 - 상태 코드가 401인 경우 특수 에러 반환")
    void parse_Fallback_401() {
        // given
        // Body가 없거나 파싱에 실패하는 401 에러 상황
        FeignException exception = new FeignException.Unauthorized(
                "Unauthorized",
                createRequest(),
                null, // Body 없음
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("A002");
        assertThat(error.message()).isEqualTo("아이디 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("Fallback 로직 - 상태 코드가 401이 아니면 파라미터로 받은 기본값 반환")
    void parse_Fallback_OtherStatus() {
        // given
        // 500 에러, Body 없음
        FeignException exception = new FeignException.InternalServerError(
                "Server Error",
                createRequest(),
                null,
                Collections.emptyMap()
        );

        // when
        FeignErrorParser.FeignError error = parser.parse(exception, "DEFAULT_CODE", "Default Message");

        // then
        assertThat(error.code()).isEqualTo("DEFAULT_CODE");
        assertThat(error.message()).isEqualTo("Default Message");
    }
}