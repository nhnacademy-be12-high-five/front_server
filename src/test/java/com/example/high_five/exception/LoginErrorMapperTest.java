package com.example.high_five.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginErrorMapperTest {

    @DisplayName("정의된 에러 코드에 맞는 메시지를 반환한다.")
    @ParameterizedTest
    @CsvSource({
            "M001, 아이디 또는 비밀번호가 올바르지 않습니다.",
            "M002, 아이디 또는 비밀번호가 올바르지 않습니다.",
            "M003, 휴면 계정입니다. 휴면 해제를 진행해주세요.",
            "M005, 휴면 계정입니다. 본인 인증 후 해제해주세요.",
            "M006, 탈퇴한 계정입니다.",
            "C001, 입력값을 다시 확인해주세요.",
            "C002, 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    })
    void toMessage_DefinedCodes(String code, String expectedMessage) {
        // when
        String actualMessage = LoginErrorMapper.toMessage(code);

        // then
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    @DisplayName("정의되지 않은 에러 코드는 기본 메시지를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "ERR_999", "", "   "})
    void toMessage_UndefinedCodes(String code) {
        // given
        String expectedDefaultMessage = "로그인에 실패했습니다.";

        // when
        String actualMessage = LoginErrorMapper.toMessage(code);

        // then
        assertThat(actualMessage).isEqualTo(expectedDefaultMessage);
    }

    @Test
    @DisplayName("입력값이 null인 경우 NullPointerException 발생 (Map.of 특성)")
    void toMessage_NullInput() {
        // Map.ofEntries로 생성된 불변 맵은 키로 null을 허용하지 않으며, getOrDefault 호출 시 NPE를 발생시킴
        assertThatThrownBy(() -> LoginErrorMapper.toMessage(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("유틸리티 클래스의 private 생성자 호출 방지 테스트")
    void privateConstructorTest() throws NoSuchMethodException {
        // given
        Constructor<LoginErrorMapper> constructor = LoginErrorMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // when & then
        // 리플렉션으로 생성자를 호출하더라도 인스턴스가 생성되긴 하지만,
        // 이 테스트는 생성자가 private임을 확인하고 호출 가능 여부를 체크하는 용도입니다.
        assertThat(constructor.getModifiers()).matches(modifier -> java.lang.reflect.Modifier.isPrivate(modifier));
        
        // 실제 호출 시 예외가 발생하지 않고 객체가 생성되는지 확인 (혹은 예외를 던지도록 설계되었다면 예외 확인)
        try {
            LoginErrorMapper instance = constructor.newInstance();
            assertThat(instance).isNotNull();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            // 생성자 내부에서 예외를 던지지 않는 한 성공적으로 인스턴스화 됨
        }
    }
}