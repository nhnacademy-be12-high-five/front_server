package com.example.high_five.dto.member.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long addressId;
    private String alias;
    private String roadAddress;
    private String detailAddress;
    private String recipient;
    private String phone;
    private String zipCode;
    private boolean isDefault;
}