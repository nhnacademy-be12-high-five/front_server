package com.example.high_five.dto.member.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberResponse {
    private Long memberId;

    private String name;

    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private String phone;
    private String status;

    private String gradeName;
}
