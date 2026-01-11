package com.example.high_five.dto;

import com.example.high_five.dto.context.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @Test
    @DisplayName("UserContext 생성 및 Getter(record) 테스트")
    void createAndGet() {
        // given
        Long id = 1L;
        String role = "USER";

        // when
        UserContext context = new UserContext(id, role);

        // then
        assertThat(context.id()).isEqualTo(id);
        assertThat(context.role()).isEqualTo(role);
    }

    @Test
    @DisplayName("isAdmin() - ADMIN 권한일 때 True 반환")
    void isAdmin_True() {
        // given
        UserContext adminContext = new UserContext(100L, "ADMIN");

        // then
        assertThat(adminContext.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("isAdmin() - ADMIN 권한이 아닐 때 False 반환")
    void isAdmin_False() {
        // given
        UserContext userContext = new UserContext(100L, "USER");
        UserContext guestContext = new UserContext(101L, "GUEST");

        // then
        assertThat(userContext.isAdmin()).isFalse();
        assertThat(guestContext.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("memberId() - id 필드 값을 정확히 반환")
    void memberId() {
        // given
        Long expectedId = 55L;
        UserContext context = new UserContext(expectedId, "USER");

        // when
        Long actualId = context.memberId();

        // then
        assertThat(actualId).isEqualTo(expectedId);
    }
}