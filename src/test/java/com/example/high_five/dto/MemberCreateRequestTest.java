package com.example.high_five.dto;

import com.example.high_five.dto.member.request.MemberCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MemberCreateRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 회원가입 요청 시 위반 사항 없음")
    void validRequest() {
        // given
        MemberCreateRequest request = new MemberCreateRequest(
                "testUser",
                "Password123!", // 영문+숫자 포함 8자 이상
                "홍길동",
                "test@example.com",
                "010-1234-5678",
                "M",
                LocalDate.now()
        );

        // when
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("필수 필드 누락 시 검증 실패")
    void nullFields() {
        // given (모든 필드 null)
        MemberCreateRequest request = new MemberCreateRequest();

        // when
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);

        // then
        // loginId, password, name, email, phone에 @NotBlank가 있으므로 최소 5개의 에러 발생
        assertThat(violations.size()).isGreaterThanOrEqualTo(5);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("아이디는 필수 입력 값입니다.")
                .contains("이메일은 필수 입력 값입니다.");
    }

    @Test
    @DisplayName("이메일 형식이 잘못된 경우 검증 실패")
    void invalidEmail() {
        // given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setLoginId("user");
        request.setPassword("Pass1234");
        request.setName("Name");
        request.setPhone("010-1234-5678");
        
        request.setEmail("invalid-email"); // @ 없는 이메일

        // when
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("올바른 이메일 형식이 아닙니다.");
    }

    @Test
    @DisplayName("비밀번호 패턴 불일치 (숫자 미포함 등) 검증 실패")
    void invalidPassword() {
        // given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setLoginId("user");
        request.setName("Name");
        request.setEmail("a@b.com");
        request.setPhone("010-1234-5678");

        request.setPassword("passwordonly"); // 숫자 없음

        // when
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("비밀번호는 8~20자여야 하며, 영문과 숫자를 반드시 포함해야 합니다.");
    }

    @Test
    @DisplayName("전화번호 형식이 잘못된 경우 검증 실패")
    void invalidPhone() {
        // given
        MemberCreateRequest request = new MemberCreateRequest();
        request.setLoginId("user");
        request.setPassword("Pass1234");
        request.setName("Name");
        request.setEmail("a@b.com");

        request.setPhone("02-123-4567"); // 01x 형식이 아님

        // when
        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("올바른 전화번호 형식이 아닙니다.");
    }
}