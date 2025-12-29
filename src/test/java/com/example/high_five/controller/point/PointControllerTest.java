package com.example.high_five.controller.point;

import com.example.high_five.common.CustomPage;
import com.example.high_five.dto.point.PointBalanceResponse;
import com.example.high_five.dto.point.PointHistoryResponse;
import com.example.high_five.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PointControllerTest {

    @InjectMocks
    private PointController pointController;

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter()
                )
                .build();
    }

    @Test
    @DisplayName("포인트 페이지 조회 - 정상")
    void pointPage_Success() throws Exception {
        // given
        String token = "access-token";
        // PointBalanceResponse(memberId, currentPoint, totalEarnedPoint)
        PointBalanceResponse balance = new PointBalanceResponse(1L, 5000L, 10000L);

        // CustomPage 객체 생성 (Setter가 없으므로 ReflectionTestUtils 사용 권장)
        CustomPage<PointHistoryResponse> historyPage = new CustomPage<>();
        ReflectionTestUtils.setField(historyPage, "content", Collections.emptyList());
        ReflectionTestUtils.setField(historyPage, "totalPages", 1);
        ReflectionTestUtils.setField(historyPage, "totalElements", 0L);

        given(memberService.getMyBalance("Bearer " + token)).willReturn(ResponseEntity.ok(balance));
        given(memberService.getMyHistory("Bearer " + token, 0, 10)).willReturn(ResponseEntity.ok(historyPage));

        // when & then
        mockMvc.perform(get("/mypage/points")
                        .cookie(new jakarta.servlet.http.Cookie("access-token", token))
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/points"))
                .andExpect(model().attribute("balance", balance))
                .andExpect(model().attributeExists("histories"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attribute("currentTab", "points"));
    }

    @Test
    @DisplayName("포인트 페이지 조회 - 서비스 예외 발생 시 기본값 반환")
    void pointPage_Exception() throws Exception {
        // given
        given(memberService.getMyBalance(anyString())).willThrow(new RuntimeException("Service Down"));

        // when & then
        mockMvc.perform(get("/mypage/points")
                        .cookie(new jakarta.servlet.http.Cookie("access-token", "token")))
                .andExpect(status().isOk())
                .andExpect(view().name("mypage/points"))
                // 예외 발생 시 Controller catch 블록에서 설정한 기본값 확인
                .andExpect(model().attributeExists("balance"))
                .andExpect(model().attribute("histories", Collections.emptyList()))
                .andExpect(model().attribute("totalPages", 0));
    }
}