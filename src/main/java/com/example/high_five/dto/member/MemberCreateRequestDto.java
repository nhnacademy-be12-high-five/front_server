package com.example.high_five.dto.member;

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
    private String phone;
    private String gender;
    private LocalDate birthDate;
}