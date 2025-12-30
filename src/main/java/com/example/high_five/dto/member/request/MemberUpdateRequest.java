package com.example.high_five.dto.member.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class MemberUpdateRequest {

    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phone;

    private Gender gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
