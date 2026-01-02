package com.example.high_five.controller.auth;

import com.example.high_five.dto.member.request.LoginRequest;
import com.example.high_five.dto.member.response.TokenDto;
import com.example.high_five.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        // @Value 필드 값 주입 (테스트용 만료 시간)
        ReflectionTestUtils.setField(authController, "accessExpirationTime", 3600000L);
        ReflectionTestUtils.setField(authController, "refreshExpirationTime", 1209600000L);
    }

    @Test
    @DisplayName("로그인 페이지 이동")
    void loginForm() throws Exception {
        mockMvc.perform(get("/member/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("member/login"));
    }

    @Test
    @DisplayName("로그인 성공 - 토큰 발급 및 쿠키 설정")
    void login_Success() throws Exception {
        // Given: 3개의 인자를 받는 생성자 사용 (profileComplete=true)
        TokenDto tokenDto = new TokenDto("access-token-val", "refresh-token-val", true);
        given(authService.login(any(LoginRequest.class))).willReturn(ResponseEntity.ok(tokenDto));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .param("loginId", "testUser")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE)); // 쿠키 설정 확인
    }

    @Test
    @DisplayName("로그인 실패 - 예외 발생 시 로그인 페이지로 리다이렉트")
    void login_Fail() throws Exception {
        given(authService.login(any(LoginRequest.class))).willThrow(new RuntimeException("Login failed"));

        mockMvc.perform(post("/auth/login")
                        .param("loginId", "test")
                        .param("password", "1234"))
                .andExpect(status().is3xxRedirection())
                // [수정] flash().attributeExists("error") 제거 -> 쿼리 파라미터로 전달됨
                .andExpect(redirectedUrl("/member/login?errorCode=C002"));
    }

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void reissue_Success() throws Exception {
        TokenDto newToken = new TokenDto("new_access", "new_refresh", true);
        given(authService.reissue("valid_refresh_token")).willReturn(ResponseEntity.ok(newToken));

        mockMvc.perform(post("/auth/reissue")
                        .cookie(new Cookie("refresh-token", "valid_refresh_token")))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("토큰 재발급 - 리프레시 토큰 없음 (401)")
    void reissue_NoCookie() throws Exception {
        mockMvc.perform(post("/auth/reissue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 (GET) - 쿠키 삭제 확인")
    void logoutByGet() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE)); // Max-Age=0 쿠키 확인

        verify(authService).logout();
    }

    @Test
    @DisplayName("소셜 로그인 콜백 - 성공 (프로필 완성)")
    void socialLoginCallback_Success() throws Exception {
        // 프로필이 완성된 상태 (isProfileComplete = true) -> 메인 페이지 리다이렉트
        TokenDto tokenDto = new TokenDto("access", "refresh", true);

        given(authService.loginSocial(anyString(), anyString())).willReturn(ResponseEntity.ok(tokenDto));

        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "auth_code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("소셜 로그인 콜백 - 성공 (프로필 미완성)")
    void socialLoginCallback_IncompleteProfile() throws Exception {
        // 프로필이 미완성 상태 (isProfileComplete = false) -> 마이페이지 정보 탭으로 리다이렉트
        TokenDto tokenDto = new TokenDto("access", "refresh", false);

        given(authService.loginSocial(anyString(), anyString())).willReturn(ResponseEntity.ok(tokenDto));

        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "auth_code"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage?tab=info")) // 리다이렉트 경로 검증
                .andExpect(flash().attributeExists("alertMessage"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("소셜 로그인 콜백 - 실패 (예외 발생)")
    void socialLoginCallback_Fail() throws Exception {
        given(authService.loginSocial(anyString(), anyString())).willThrow(new RuntimeException("Social Login Fail"));

        mockMvc.perform(get("/login/oauth2/code/google")
                        .param("code", "auth_code"))
                .andExpect(status().is3xxRedirection())
                // [수정] flash().attributeExists("error") 제거 -> 쿼리 파라미터로 전달됨
                .andExpect(redirectedUrl("/member/login?errorCode=C002"));
    }
}