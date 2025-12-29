package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.DormantRequest;
import com.example.high_five.dto.member.request.EmailRequest;
import com.example.high_five.dto.member.request.EmailVerifyRequest;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    @InjectMocks
    private AuthRestController authRestController;

    @Mock
    private AuthService authService;

    @Mock
    private MemberService memberService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // [핵심 수정] StringHttpMessageConverter를 UTF-8로 강제 설정하여 등록
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8), // 문자열 반환 시 UTF-8 처리
                        new MappingJackson2HttpMessageConverter() // JSON 처리
                )
                .build();
    }

    @Test
    @DisplayName("이메일 발송 - 성공")
    void sendEmail_Success() throws Exception {
        mockMvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("인증번호가 발송되었습니다."));

        verify(authService).sendEmail(any(EmailRequest.class));
    }

    @Test
    @DisplayName("이메일 발송 - FeignException (JSON 에러 메시지 파싱)")
    void sendEmail_FeignException() throws Exception {
        // given
        String errorJson = "{\"message\":\"Invalid Email\"}";
        Request request = Request.create(HttpMethod.POST, "url", Map.of(), errorJson.getBytes(StandardCharsets.UTF_8), null, null);
        FeignException exception = new FeignException.BadRequest("Bad Request", request, errorJson.getBytes(StandardCharsets.UTF_8), Map.of());

        doThrow(exception).when(authService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid Email"));
    }

    @Test
    @DisplayName("이메일 인증 확인")
    void verifyEmail() throws Exception {
        given(authService.verifyEmail(any(EmailVerifyRequest.class))).willReturn(ResponseEntity.ok("Success"));

        mockMvc.perform(post("/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\", \"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Success"));
    }

    @Test
    @DisplayName("아이디 중복 체크 - 사용 가능(true)")
    void checkId_Success() throws Exception {
        given(authService.checkId("newId")).willReturn(ResponseEntity.ok(true));

        mockMvc.perform(get("/auth/check-id")
                        .param("loginId", "newId"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("아이디 중복 체크 - 예외 발생 (500)")
    void checkId_Exception() throws Exception {
        given(authService.checkId(anyString())).willThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/auth/check-id")
                        .param("loginId", "error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("아이디 찾기 이메일 발송")
    void sendFindIdCode() throws Exception {
        mockMvc.perform(post("/auth/email/find-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("인증번호가 발송되었습니다."));

        verify(authService).sendFindIdCode(any(EmailRequest.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송")
    void sendPasswordResetCode() throws Exception {
        mockMvc.perform(post("/auth/email/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("인증번호가 발송되었습니다."));

        verify(authService).sendPasswordResetCode(any(EmailRequest.class));
    }

    @Test
    @DisplayName("휴면 계정 인증 코드 발송")
    void sendDormantCode() throws Exception {
        given(memberService.checkDormantMember(any())).willReturn(ResponseEntity.ok(true));

        mockMvc.perform(post("/auth/dormant/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dormant@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("인증번호가 발송되었습니다."));

        verify(memberService).checkDormantMember(any(DormantRequest.class));
        verify(memberService).sendDormantEmail(any(EmailRequest.class));
    }

    @Test
    @DisplayName("휴면 계정 해제 인증")
    void verifyAndActivate() throws Exception {
        mockMvc.perform(post("/auth/dormant/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("휴면 상태가 해제되었습니다."));

        verify(memberService).activateDormant(any(DormantRequest.class));
    }

    @Test
    @DisplayName("휴면 계정 해제 인증 실패")
    void verifyAndActivate_Fail() throws Exception {
        doThrow(new RuntimeException("Fail")).when(memberService).activateDormant(any());

        mockMvc.perform(post("/auth/dormant/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("인증번호가 틀렸거나 오류가 발생했습니다."));
    }
}