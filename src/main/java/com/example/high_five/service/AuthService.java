package com.example.high_five.service;

import com.example.high_five.dto.member.LoginRequest;
import com.example.high_five.dto.member.LoginResponse;
import com.example.high_five.dto.member.MemberCreateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-server", contextId = "authClient", url = "http://localhost:8082")
public interface AuthService {

    @PostMapping("/api/auth/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/api/auth/signup")
    ResponseEntity<MemberCreateRequestDto> signup(@RequestBody MemberCreateRequestDto memberRegisterRequestDto);

    @GetMapping("/api/auth/my-page")
    ResponseEntity<String> mypage();
}