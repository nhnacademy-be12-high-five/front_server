package com.example.high_five.exception;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignExceptionHandlerTest {

    private final FeignExceptionHandler exceptionHandler = new FeignExceptionHandler();

    // 공통 Request 생성을 위한 헬퍼 메소드
    private Request createRequest() {
        return Request.create(HttpMethod.GET, "/api/test", Map.of(), null, null, null);
    }

    @Test
    @DisplayName("FeignException - 정상적인 에러 응답(JSON) 파싱")
    void handleFeignException_ValidJson() {
        // given
        String errorJson = "{\"code\":\"BAD_REQUEST\", \"message\":\"잘못된 요청입니다.\"}";
        
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                errorJson.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다.");
    }

    @Test
    @DisplayName("FeignException - JSON 파싱 실패 시 기본 메시지 반환")
    void handleFeignException_InvalidJson() {
        // given
        String rawBody = "Internal Server Error Occurred"; // JSON 아님
        
        FeignException exception = new FeignException.InternalServerError(
                "Server Error",
                createRequest(),
                rawBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        // 파싱 실패 시 지정된 기본값 확인
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
        assertThat(response.getBody().message()).isEqualTo("외부 서비스 호출 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("FeignException - 바디가 null인 경우 기본 메시지 반환")
    void handleFeignException_NullBody() {
        // given
        FeignException exception = new FeignException.NotFound(
                "Not Found",
                createRequest(),
                null,
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
    }

    @Test
    @DisplayName("FeignException - 바디가 빈 문자열(\"\")인 경우 파싱 스킵 및 기본 메시지 반환")
    void handleFeignException_EmptyStringBody() {
        // given
        String emptyBody = "";
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                emptyBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
    }

    @Test
    @DisplayName("FeignException - 바디가 공백(\"   \")인 경우 파싱 스킵 및 기본 메시지 반환")
    void handleFeignException_WhitespaceBody() {
        // given
        String whitespaceBody = "   ";
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                whitespaceBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
    }

    @Test
    @DisplayName("FeignException - JSON 필드가 일부만 있는 경우(message 누락)")
    void handleFeignException_PartialJson() {
        // given
        // message 필드가 없는 경우
        String json = "{\"code\":\"PARTIAL_ERROR\"}"; 
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                createRequest(),
                json.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PARTIAL_ERROR");
        // ObjectMapper는 없는 필드를 null로 매핑
        assertThat(response.getBody().message()).isNull(); 
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 405, 500, 502, 503})
    @DisplayName("FeignException - 다양한 HTTP 상태 코드 전파 확인")
    void handleFeignException_VariousStatusCodes(int status) {
        // given
        // 익명 클래스로 특정 상태 코드를 가진 FeignException 생성
        FeignException exception = new FeignException(status, "Error", createRequest(), null, null) {};

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
    }

    @Test
    @DisplayName("FeignException - 정의되지 않은 상태 코드(999)는 500 Internal Server Error 처리")
    void handleFeignException_InvalidStatusCode() {
        // given
        // HttpStatus enum에 없는 코드 (999)
        FeignException exception = new FeignException(999, "Unknown Code", createRequest(), null, null) {};

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        // HttpStatus.resolve(999) == null 이므로 500 반환 예상
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
    }

    @Test
    @DisplayName("일반 Exception 처리 - 500 에러 및 FE001 코드 반환")
    void handleException() {
        // given
        Exception e = new RuntimeException("Unexpected Error");

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleException(e);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FE001");
        assertThat(response.getBody().message()).isEqualTo("프론트 서버 내부 오류가 발생했습니다.");
    }
}