package com.example.high_five.exception;

import java.util.Map;

/**
 * 로그인 실패 시 에러코드 → 사용자 메시지 변환 책임을 담당하는 매퍼
 * - 외부(Member Server) 에러코드에 직접 의존하지 않고
 * - 화면 UX 관점에서 메시지를 통제하기 위함
 */
public final class LoginErrorMapper {

    private static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("M001", "아이디 또는 비밀번호가 올바르지 않습니다."),
            Map.entry("M002", "아이디 또는 비밀번호가 올바르지 않습니다."),
            Map.entry("M003", "휴면 계정입니다. 휴면 해제를 진행해주세요."),
            Map.entry("M005", "휴면 계정입니다. 본인 인증 후 해제해주세요."),
            Map.entry("M006", "탈퇴한 계정입니다."),
            Map.entry("C001", "입력값을 다시 확인해주세요."),
            Map.entry("C002", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
    );

    private LoginErrorMapper() {}

    public static String toMessage(String code) {
        return MAP.getOrDefault(code, "로그인에 실패했습니다.");
    }
}