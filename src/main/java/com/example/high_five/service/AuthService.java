package com.example.high_five.service;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.request.LoginResponse;
import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "gateway-server", contextId = "authClient", url = "http://localhost:8000")
public interface AuthService {

    @PostMapping("/api/auth/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest);

    @PostMapping("/api/auth/signup")
    ResponseEntity<MemberCreateRequestDto> signup(@RequestBody MemberCreateRequestDto memberRegisterRequestDto);

}