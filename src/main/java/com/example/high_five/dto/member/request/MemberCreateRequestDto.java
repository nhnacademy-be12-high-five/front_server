package com.example.high_five.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequestDto {
    private String loginId;
    private String password;
    private String name;
    private String email;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;
    private String gender;
    private LocalDate birthDate;
}