package com.example.high_five.dto.member.request;// [Backend & Frontend] dto.request 패키지
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    private String loginId;
    private String email;
    private String authCode;
    private String newPassword;
}