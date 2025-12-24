package com.example.high_five.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class FeignExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException e) {

        HttpStatus status = HttpStatus.resolve(e.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse =
                new ErrorResponse("FEIGN_ERROR", "외부 서비스 호출 중 오류가 발생했습니다.");

        String content = e.contentUTF8();
        if (content != null && !content.isBlank()) {
            try {
                errorResponse = objectMapper.readValue(content, ErrorResponse.class);
            } catch (Exception parseException) {
                log.warn("Feign error body 파싱 실패: {}", content);
            }
        }

        log.warn("FeignException 전달: status={}, body={}", status, content);

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Frontend Server Exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("FE001", "프론트 서버 내부 오류가 발생했습니다."));
    }

    public record ErrorResponse(String code, String message) {}
}
