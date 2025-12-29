package com.example.high_five.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CommonPageResponseTest {

    @Test
    @DisplayName("isFirst()와 isLast() 로직 검증")
    void checkPageLogic() {
        // given
        CommonPageResponse<String> response = new CommonPageResponse<>();

        // Setter가 없으므로 ReflectionTestUtils로 강제 주입
        ReflectionTestUtils.setField(response, "pageNumber", 0);
        ReflectionTestUtils.setField(response, "totalPages", 5);

        // then: 0페이지면 첫 페이지여야 함
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();

        // when: 마지막 페이지로 변경 (4페이지 = 5 - 1)
        ReflectionTestUtils.setField(response, "pageNumber", 4);

        // then: 마지막 페이지여야 함
        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isTrue();
    }
}