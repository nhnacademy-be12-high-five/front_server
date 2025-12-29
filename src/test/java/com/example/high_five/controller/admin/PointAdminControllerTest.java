package com.example.high_five.controller.admin;

import com.example.high_five.dto.point.PointAdminAdjustmentRequest;
import com.example.high_five.dto.point.PointAdminPolicyRequest;
import com.example.high_five.dto.point.PointAdminPolicyResponse;
import com.example.high_five.service.MemberService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PointAdminControllerTest {

    @InjectMocks
    private PointAdminController pointAdminController;

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointAdminController).build();
    }

    @Test
    @DisplayName("포인트 정책 조회")
    void getPointPage() throws Exception {
        PointAdminPolicyResponse policy = new PointAdminPolicyResponse(10, 100, 5000, LocalDateTime.now());
        given(memberService.getPolicy()).willReturn(ResponseEntity.ok(policy));

        mockMvc.perform(get("/admin/points/policy"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("policy", policy));
    }

    @Test
    @DisplayName("포인트 정책 수정 - 성공")
    void updatePointPolicy_Success() throws Exception {
        mockMvc.perform(post("/admin/points/policy")
                        .flashAttr("pointAdminPolicyRequest", new PointAdminPolicyRequest()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"));

        verify(memberService).updatePolicy(any());
    }

    @Test
    @DisplayName("포인트 수동 조정 - 지급(양수)")
    void adjustmentMemberPoint_Positive() throws Exception {
        PointAdminAdjustmentRequest request = new PointAdminAdjustmentRequest(1L, 1000L, "Bonus");

        mockMvc.perform(post("/admin/points/adjustment")
                        .flashAttr("pointAdminAdjustmentRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message2", "포인트 지급 처리가 완료되었습니다."));
    }

    @Test
    @DisplayName("포인트 수동 조정 - 차감(음수)")
    void adjustmentMemberPoint_Negative() throws Exception {
        PointAdminAdjustmentRequest request = new PointAdminAdjustmentRequest(1L, -1000L, "Penalty");

        mockMvc.perform(post("/admin/points/adjustment")
                        .flashAttr("pointAdminAdjustmentRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("message2", "포인트 차감 처리가 완료되었습니다."));
    }

    @Test
    @DisplayName("포인트 수동 조정 - 회원 없음(404)")
    void adjustmentMemberPoint_NotFound() throws Exception {
        Request feignReq = Request.create(Request.HttpMethod.POST, "url", Map.of(), null, null, null);
        doThrow(new FeignException.NotFound("Not Found", feignReq, null, null))
                .when(memberService).manualAdjustment(any());

        mockMvc.perform(post("/admin/points/adjustment")
                        .flashAttr("pointAdminAdjustmentRequest", new PointAdminAdjustmentRequest(1L, 100L, "Test")))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage2", "존재하지 않는 회원입니다."));
    }

    @Test
    @DisplayName("인덱스 리다이렉트")
    void index() throws Exception {
        mockMvc.perform(get("/admin/points"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/points/policy"));
    }
}