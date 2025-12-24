package com.example.high_five.service;

import com.example.high_five.dto.member.request.AddressRequest;
import com.example.high_five.dto.member.response.AddressListResponse;
import com.example.high_five.dto.member.response.AddressResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gateway-server", contextId = "addressClient", url = "${gateway.uri}")
public interface AddressService {

    // 1. 목록 조회 (백엔드가 AddressListResponse로 감싸서 줌)
    @GetMapping("/api/address")
    ResponseEntity<AddressListResponse> getAddressList();

    // 2. 단건 조회 (수정 화면용)
    @GetMapping("/api/address/{address-id}")
    ResponseEntity<AddressResponse> getAddress(@PathVariable("address-id") Long addressId);

    // 3. 등록
    @PostMapping("/api/address")
    ResponseEntity<AddressResponse> createAddress(@RequestBody AddressRequest request);

    // 4. 수정 (백엔드가 PATCH 메서드 사용함!)
    @PutMapping("/api/address/{address-id}")
    ResponseEntity<AddressResponse> updateAddress(@PathVariable("address-id") Long addressId,
                                                  @RequestBody AddressRequest request);

    // 5. 삭제
    @DeleteMapping("/api/address/{address-id}")
    ResponseEntity<Void> deleteAddress(@PathVariable("address-id") Long addressId);

    // 6. 기본 배송지 설정
    @PostMapping("/api/address/{address-id}/default")
    ResponseEntity<AddressResponse> setDefaultAddress(@PathVariable("address-id") Long addressId);
}