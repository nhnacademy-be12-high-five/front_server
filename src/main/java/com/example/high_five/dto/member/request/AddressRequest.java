package com.example.high_five.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "별칭은 필수 입력 값입니다.")
    private String alias;

    @NotBlank(message = "도로명 주소는 필수 입력 값입니다.")
    private String roadAddress;

    @NotBlank(message = "상세 주소는 필수 입력 값입니다.")
    private String detailAddress;

    @NotBlank(message = "받는 분 성함은 필수 입력 값입니다.")
    private String recipient;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 전화번호 형식이 아닙니다.")
    private String phone;

    @NotBlank(message = "우편번호는 필수 입력 값입니다.")
    private String zipCode;

    private boolean defaultAddress;
}