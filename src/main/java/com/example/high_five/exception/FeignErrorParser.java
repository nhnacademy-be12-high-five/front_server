package com.example.high_five.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class FeignErrorParser {

    private final ObjectMapper objectMapper;

    public record FeignError(String code, String message) {}

    public FeignError parse(FeignException e, String defaultCode, String defaultMessage) {
        String body = readBody(e);

        if (body == null || body.isBlank()) {
            return statusFallback(e, defaultCode, defaultMessage);
        }

        try {
            JsonNode node = objectMapper.readTree(body);
            String code = node.has("code") ? node.get("code").asText() : defaultCode;
            String msg  = node.has("message") ? node.get("message").asText() : defaultMessage;
            return new FeignError(code, msg);
        } catch (Exception ignore) {
            return statusFallback(e, defaultCode, defaultMessage);
        }
    }

    private String readBody(FeignException e) {
        String body = e.contentUTF8();
        if (body != null && !body.isBlank()) return body;

        return e.responseBody()
                .map(bb -> StandardCharsets.UTF_8.decode(bb).toString())
                .orElse("");
    }

    private FeignError statusFallback(FeignException e, String defaultCode, String defaultMessage) {
        if (e.status() == 401) {
            return new FeignError("A002", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return new FeignError(defaultCode, defaultMessage);
    }
}