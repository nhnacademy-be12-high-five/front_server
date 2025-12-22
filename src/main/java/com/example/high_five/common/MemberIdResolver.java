package com.example.high_five.common;

import com.example.high_five.common.utils.JwtPayloadParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MemberIdResolver {

    private final HttpServletRequest request;

    public Long resolveRequired() {
        String token = getCookieValue("access-token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Map<String, Object> claims = JwtPayloadParser.parseClaims(token);

        // 팀마다 키 이름이 다를 수 있어서 안전하게 여러 후보를 확인
        Object v = firstNonNull(
                claims.get("memberId"),
                claims.get("member_id"),
                claims.get("id"),
                claims.get("userId"),
                claims.get("user_id")
        );

        if (v == null) {
            throw new IllegalStateException("토큰에 회원 식별자(memberId)가 없습니다.");
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(v));
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private String getCookieValue(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
