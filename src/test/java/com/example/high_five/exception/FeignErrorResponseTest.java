package com.example.high_five.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FeignErrorResponseTest {

    @Test
    @DisplayName("FeignErrorResponse 생성 및 필드 조회 테스트")
    void createAndGetFields() {
        // given
        String expectedCode = "ERR_TEST";
        String expectedMessage = "테스트 에러 메시지";

        // when
        FeignErrorResponse response = new FeignErrorResponse(expectedCode, expectedMessage);

        // then
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo(expectedCode);
        assertThat(response.message()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("FeignErrorResponse 동등성(Equals) 및 해시코드 테스트")
    void equalsAndHashCode() {
        // given
        FeignErrorResponse response1 = new FeignErrorResponse("ERR_001", "Error");
        FeignErrorResponse response2 = new FeignErrorResponse("ERR_001", "Error");
        FeignErrorResponse differentResponse = new FeignErrorResponse("ERR_002", "Other Error");

        // then
        // 내용이 같은 두 객체는 같아야 함 (Value Object 특성)
        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());

        // 내용이 다른 객체는 달라야 함
        assertThat(response1).isNotEqualTo(differentResponse);
    }

    @Test
    @DisplayName("FeignErrorResponse toString 포함 여부 테스트")
    void toStringContainsValues() {
        // given
        String code = "ToString_Code";
        String message = "ToString_Message";
        FeignErrorResponse response = new FeignErrorResponse(code, message);

        // when
        String toStringResult = response.toString();

        // then
        // toString 결과에 필드 값이 포함되어 있는지 확인 (디버깅 용이성)
        assertThat(toStringResult)
                .contains(code)
                .contains(message)
                .contains("FeignErrorResponse");
    }
}