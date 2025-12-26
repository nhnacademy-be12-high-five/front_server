package com.example.high_five.dto.member.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DormantRequest {
    private String loginId;
    private String email;
    private String authCode;
    private String type;
}