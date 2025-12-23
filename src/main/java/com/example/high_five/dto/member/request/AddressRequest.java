package com.example.high_five.dto.member.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    // HTML의 name="alias" 와 매칭
    private String alias;

    // HTML의 name="roadAddress" 와 매칭 (★중요★ 이게 틀려서 에러 났음)
    private String roadAddress;

    // HTML의 name="detailAddress" 와 매칭
    private String detailAddress;

    // (참고) 백엔드에는 recipient, phone, zipCode, isDefault 필드가 없어서
    // 보내도 저장이 안 될 수 있습니다. 일단 에러를 잡기 위해 위 3개는 필수입니다.
    private String recipient;
    private String phone;
    private String zipCode;
    private boolean defaultAddress;
}