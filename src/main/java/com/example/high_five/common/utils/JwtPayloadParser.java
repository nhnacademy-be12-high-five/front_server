package com.example.high_five.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Map;

@Slf4j
public class JwtPayloadParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> parseClaims(String token) {
        try {
            if (token == null || token.isBlank()) {
                return Map.of();
            }

            String cleanToken = token.startsWith("Bearer ") ? token.substring(7) : token;

            String[] parts = cleanToken.split("\\.");
            if (parts.length != 3) {
                return Map.of();
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("JWT 파싱 실패: {}", e.getMessage());
            return Map.of();
        }
    }
}