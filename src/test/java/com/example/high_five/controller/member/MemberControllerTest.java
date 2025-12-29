package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.MemberCreateRequestDto;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @InjectMocks
    private MemberController memberController;

    @Mock
    private MemberService memberService;
    @Mock
    private AuthService authService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(memberController).build();
    }

    @Test
    @DisplayName("회원가입 페이지 이동")
    void signupForm() throws Exception {
        mockMvc.perform(get("/member/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/signup"));
    }

    @Test
    @DisplayName("회원가입 처리 - 성공")
    void signup_Success() throws Exception {
        mockMvc.perform(post("/member/signup")
                        .flashAttr("memberCreateRequestDto", new MemberCreateRequestDto("id", "pw", "name", null, null, null, null)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));

        verify(authService).signup(any(MemberCreateRequestDto.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 실패")
    void signup_Fail() throws Exception {
        doThrow(new RuntimeException("Fail")).when(authService).signup(any());

        mockMvc.perform(post("/member/signup")
                        .flashAttr("memberCreateRequestDto", new MemberCreateRequestDto()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/signup?error=true"));
    }

    @Test
    @DisplayName("마이페이지 조회 - 성공")
    void myPage_Success() throws Exception {
        MemberResponse response = new MemberResponse(
                1L, "testUser", "test@test.com", LocalDate.of(2000, 1, 1),
                "010-1234-5678", "ACTIVE", "GOLD"
        );
        given(memberService.getMyInfo()).willReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/mypage"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/myinfo"))
                .andExpect(model().attribute("myInfo", response));
    }

    @Test
    @DisplayName("마이페이지 조회 - 실패 (로그인 리다이렉트)")
    void myPage_Fail() throws Exception {
        // [수정] NPE 방지를 위해 Request 객체 생성
        Request request = Request.create(HttpMethod.GET, "/api/my-info", Map.of(), null, null, null);
        given(memberService.getMyInfo()).willThrow(new FeignException.Unauthorized("401 Unauthorized", request, null, null));

        mockMvc.perform(get("/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    void updateMember_Success() throws Exception {
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .name("Updated Name")
                .email("update@test.com")
                .phone("010-9999-9999")
                .build();

        mockMvc.perform(post("/mypage/update")
                        .flashAttr("memberUpdateRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attribute("alertMessage", "수정되었습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 - 실패 (Feign 메시지 파싱)")
    void updateMember_FeignException() throws Exception {
        String errorJson = "{\"message\":\"Validation Error\"}";
        // [수정] Request 객체 생성 시 body 포함
        Request request = Request.create(HttpMethod.PUT, "/api/members", Map.of(), errorJson.getBytes(StandardCharsets.UTF_8), null, null);
        FeignException exception = new FeignException.BadRequest("Bad Request", request, errorJson.getBytes(StandardCharsets.UTF_8), Map.of());

        doThrow(exception).when(memberService).updateMember(any());

        mockMvc.perform(post("/mypage/update")
                        .flashAttr("memberUpdateRequest", new MemberUpdateRequest()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"))
                .andExpect(flash().attribute("alertMessage", "Validation Error"));
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공 (쿠키 삭제)")
    void withdrawMember_Success() throws Exception {
        mockMvc.perform(post("/mypage/withdraw")
                        .cookie(new Cookie("access-token", "val"), new Cookie("refresh-token", "val")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "탈퇴되었습니다."))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(memberService).withdrawMember();
    }
}