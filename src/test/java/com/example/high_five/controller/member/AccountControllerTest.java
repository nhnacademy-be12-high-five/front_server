package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.dto.member.request.PasswordResetRequest;
import com.example.high_five.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AuthService authService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // [수정] 한글 깨짐 방지를 위한 메시지 컨버터 및 필터 추가
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("아이디 찾기 페이지 이동")
    void findIdForm() throws Exception {
        mockMvc.perform(get("/account/find/id"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/find-id"));
    }

    @Test
    @DisplayName("비밀번호 찾기 페이지 이동")
    void findPasswordForm() throws Exception {
        mockMvc.perform(get("/account/find/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/find-password"));
    }

    @Test
    @DisplayName("아이디 찾기 API - 성공")
    void findId_Success() throws Exception {
        given(authService.findId(any(EmailVerifyRequest.class))).willReturn(ResponseEntity.ok("testId"));

        mockMvc.perform(post("/account/api/find/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\", \"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("testId"));
    }

    @Test
    @DisplayName("아이디 찾기 API - FeignException (에러 메시지 파싱)")
    void findId_FeignException() throws Exception {
        String errorJson = "{\"message\":\"User Not Found\"}";
        Request request = Request.create(HttpMethod.POST, "url", Map.of(), errorJson.getBytes(StandardCharsets.UTF_8), null, null);
        FeignException exception = new FeignException.NotFound("Not Found", request, errorJson.getBytes(StandardCharsets.UTF_8), Map.of());

        given(authService.findId(any())).willThrow(exception);

        mockMvc.perform(post("/account/api/find/id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User Not Found"));
    }

    @Test
    @DisplayName("비밀번호 재설정 API - 성공")
    void resetPassword_Success() throws Exception {
        mockMvc.perform(post("/account/api/find/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\", \"password\":\"newPass\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호가 성공적으로 변경되었습니다.")); // 이제 깨지지 않음
    }
}