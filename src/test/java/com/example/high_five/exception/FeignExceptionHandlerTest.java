package com.example.high_five.exception;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignExceptionHandlerTest {

    private final FeignExceptionHandler exceptionHandler = new FeignExceptionHandler();

    @Test
    @DisplayName("FeignException - 정상적인 에러 응답(JSON) 파싱")
    void handleFeignException_ValidJson() {
        // given
        String errorJson = "{\"code\":\"BAD_REQUEST\", \"message\":\"잘못된 요청입니다.\"}";
        Request request = Request.create(HttpMethod.GET, "/api/test", Map.of(), null, null, null);

        // 400 Bad Request 발생 상황 시뮬레이션
        FeignException exception = new FeignException.BadRequest(
                "Bad Request",
                request,
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
        Request request = Request.create(HttpMethod.POST, "/api/test", Map.of(), null, null, null);

        FeignException exception = new FeignException.InternalServerError(
                "Server Error",
                request,
                rawBody.getBytes(StandardCharsets.UTF_8),
                Collections.emptyMap()
        );

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("FEIGN_ERROR");
        assertThat(response.getBody().message()).isEqualTo("외부 서비스 호출 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("FeignException - 바디가 없는 경우 기본 메시지 반환")
    void handleFeignException_NullBody() {
        // given
        Request request = Request.create(HttpMethod.GET, "/api/test", Map.of(), null, null, null);
        // 바디가 null인 예외
        FeignException exception = new FeignException.NotFound(
                "Not Found",
                request,
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
    @DisplayName("FeignException - 알 수 없는 상태 코드는 500으로 처리")
    void handleFeignException_UnknownStatus() {
        // given
        Request request = Request.create(HttpMethod.GET, "/api/test", Map.of(), null, null, null);
        // 상태 코드 0 (FeignException 기본 생성자 등에서 발생 가능) 또는 정의되지 않은 코드
        FeignException exception = new FeignException(0, "Unknown Error", request, null, null) {};

        // when
        ResponseEntity<FeignExceptionHandler.ErrorResponse> response = exceptionHandler.handleFeignException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("일반 Exception 처리")
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