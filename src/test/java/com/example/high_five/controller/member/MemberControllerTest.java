package com.example.high_five.controller.member;

import com.example.high_five.dto.member.request.MemberCreateRequest;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.exception.FeignErrorParser;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    @Mock
    private FeignErrorParser feignErrorParser; // [중요] Mock 주입

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
    @DisplayName("회원가입 처리 - 성공 (리다이렉트)")
    void signup_Success() throws Exception {
        // [수정] 유효성 검사(@Valid)를 통과할 수 있는 완벽한 데이터 준비
        MemberCreateRequest validRequest = new MemberCreateRequest(
                "validUser",          // loginId
                "Password123",        // password (8자 이상, 영문+숫자)
                "테스트유저",           // name
                "test@example.com",   // email
                "010-1234-5678",      // phone
                "MALE",               // gender
                LocalDate.of(2000, 1, 1) // birthDate
        );

        mockMvc.perform(post("/member/signup")
                        .flashAttr("memberCreateRequest", validRequest)) // 데이터 주입
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"));

        verify(authService).signup(any(MemberCreateRequest.class));
    }

    @Test
    @DisplayName("회원가입 처리 - 실패 (예외 발생 시 다시 폼으로)")
    void signup_Fail() throws Exception {
        // [수정] 실패 테스트라도 '입력값' 자체는 유효해야 서비스 로직까지 도달함
        MemberCreateRequest validRequest = new MemberCreateRequest(
                "validUser",
                "Password123",
                "테스트유저",
                "test@example.com",
                "010-1234-5678",
                "MALE",
                LocalDate.now()
        );

        // 서비스에서 예외가 터지도록 설정
        doThrow(FeignException.class).when(authService).signup(any());

        // FeignErrorParser 동작 설정 (이전과 동일)
        given(feignErrorParser.parse(any(), anyString(), anyString()))
                .willReturn(new FeignErrorParser.FeignError("FAIL", "가입 실패"));

        mockMvc.perform(post("/member/signup")
                        .flashAttr("memberCreateRequest", validRequest)) // 유효한 객체 전달
                .andExpect(status().isOk()) // 예외 발생 시 폼으로 돌아옴 (200 OK)
                .andExpect(view().name("member/signup"))
                .andExpect(model().attribute("errorMessage", "가입 실패"));
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
    @DisplayName("회원 정보 수정 - 성공 (리다이렉트 + 쿼리파라미터)")
    void updateMember_Success() throws Exception {
        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .name("Updated Name")
                .email("update@test.com")
                .phone("010-9999-9999")
                .build();

        mockMvc.perform(post("/mypage/update")
                        .flashAttr("memberUpdateRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage?alertCode=MP200")); // 컨트롤러 로직 반영
    }

    @Test
    @DisplayName("회원 정보 수정 - 실패 (Feign 예외 발생 시 뷰 반환)")
    void updateMember_FeignException() throws Exception {
        // given
        String errorJson = "{\"message\":\"Error\"}";
        Request request = Request.create(HttpMethod.PUT, "/api/members", Map.of(), errorJson.getBytes(StandardCharsets.UTF_8), null, null);
        FeignException exception = new FeignException.BadRequest("Bad Request", request, errorJson.getBytes(StandardCharsets.UTF_8), Map.of());

        doThrow(exception).when(memberService).updateMember(any());

        // [핵심 2] FeignError 객체 반환 Stubbing
        given(feignErrorParser.parse(any(), anyString(), anyString()))
                .willReturn(new FeignErrorParser.FeignError("E400", "수정 실패 메시지"));

        // loadMyInfoData() 호출 방지 또는 Mocking (예외 터져도 catch 블록 있어서 괜찮음)

        // when & then
        mockMvc.perform(post("/mypage/update")
                        .flashAttr("memberUpdateRequest", new MemberUpdateRequest()))
                .andExpect(status().isOk()) // [중요] 실패 시 리다이렉트가 아니라 뷰 리턴임
                .andExpect(view().name("mypage/myinfo"))
                .andExpect(model().attribute("errorMessage", "수정 실패 메시지"));
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공 (쿠키 삭제 및 리다이렉트)")
    void withdrawMember_Success() throws Exception {
        mockMvc.perform(post("/mypage/withdraw")
                        .cookie(new Cookie("access-token", "val"), new Cookie("refresh-token", "val")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?alertCode=MP201")) // 컨트롤러 로직 반영
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(memberService).withdrawMember();
    }
}