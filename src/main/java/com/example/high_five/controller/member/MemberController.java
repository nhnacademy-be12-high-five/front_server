package com.example.high_five.controller.member;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.request.MemberCreateRequest;
import com.example.high_five.dto.member.request.MemberUpdateRequest;
import com.example.high_five.dto.member.response.MemberResponse;
import com.example.high_five.exception.FeignErrorParser;
import com.example.high_five.service.AuthService;
import com.example.high_five.service.MemberService;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid; // ★ 필수 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // ★ 필수 import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;
    private final FeignErrorParser feignErrorParser;

    // [GET] 회원가입 폼
    @GetMapping("/member/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberCreateRequest", new MemberCreateRequest());
        return "member/signup";
    }

    // [POST] 회원가입 (프론트에서 먼저 검사!)
    @PostMapping("/member/signup")
    public String signup(@ModelAttribute @Valid MemberCreateRequest requestDto,
                         BindingResult bindingResult, // ★ @Valid 바로 뒤에 와야 함
                         Model model) {

        // 1. 프론트에서 유효성 검사 (형식 틀리면 여기서 바로 컷!)
        if (bindingResult.hasErrors()) {
            // 에러 메시지 중 첫 번째 것만 뽑아서 가져옴
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();

            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("memberCreateRequest", requestDto); // 입력값 유지
            return "member/signup"; // 리다이렉트 안 함!
        }

        try {
            // 2. 형식이 맞으면 백엔드로 보냄
            authService.signup(requestDto);
            return "redirect:/member/login";

        } catch (FeignException e) {
            // 백엔드 에러 (아이디 중복 등) 처리
            FeignErrorParser.FeignError error = feignErrorParser.parse(e, "SignUpFail", "회원가입 실패");
            model.addAttribute("errorMessage", error.message());
            model.addAttribute("memberCreateRequest", requestDto);
            return "member/signup";
        } catch (Exception e) {
            log.error("시스템 오류", e);
            model.addAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            model.addAttribute("memberCreateRequest", requestDto);
            return "member/signup";
        }
    }

    // [GET] 마이페이지
    @LoginRequired
    @GetMapping("/mypage")
    public String myPage(Model model,
                         @RequestParam(value = "alertCode", required = false) String alertCode) {
        loadMyInfoData(model);

        if (model.containsAttribute("myInfo")) {
            MemberResponse myInfo = (MemberResponse) model.getAttribute("myInfo");
            MemberUpdateRequest updateReq = new MemberUpdateRequest();
            updateReq.setName(myInfo.getName());
            updateReq.setEmail(myInfo.getEmail());
            updateReq.setPhone(myInfo.getPhone());
            updateReq.setBirthDate(myInfo.getBirthDate());
            // 필요한 필드 매핑...

            model.addAttribute("memberUpdateRequest", updateReq);
        }

        model.addAttribute("alertCode", alertCode);
        model.addAttribute("currentTab", "info");
        return "mypage/myinfo";
    }

    // [POST] 회원수정 (프론트에서 먼저 검사!)
    @LoginRequired
    @PostMapping("/mypage/update")
    public String updateMember(@ModelAttribute @Valid MemberUpdateRequest request,
                               BindingResult bindingResult, // ★ @Valid 바로 뒤에!
                               Model model,
                               RedirectAttributes rttr) {

        // 1. 프론트 검사: 형식 틀리면 여기서 컷
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();

            model.addAttribute("errorMessage", errorMsg);
            model.addAttribute("memberUpdateRequest", request); // 입력값 유지
            model.addAttribute("currentTab", "info");

            loadMyInfoData(model); // 배경 데이터 복구
            return "mypage/myinfo"; // 리다이렉트 X
        }

        try {
            // 2. 백엔드 요청
            memberService.updateMember(request);
            rttr.addAttribute("alertCode", "MP200");
            return "redirect:/mypage";

        } catch (FeignException e) {
            FeignErrorParser.FeignError fe = feignErrorParser.parse(e, "C002", "수정 실패");

            model.addAttribute("errorMessage", fe.message());
            model.addAttribute("memberUpdateRequest", request);
            model.addAttribute("currentTab", "info");

            loadMyInfoData(model);
            return "mypage/myinfo";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "시스템 오류");
            model.addAttribute("memberUpdateRequest", request);
            loadMyInfoData(model);
            return "mypage/myinfo";
        }
    }

    // [POST] 탈퇴 (입력폼 없어서 Validation 불필요)
    @LoginRequired
    @PostMapping("/mypage/withdraw")
    public String withdrawMember(HttpServletResponse response, RedirectAttributes rttr) {
        try {
            memberService.withdrawMember();
            deleteCookie(response, "access-token");
            deleteCookie(response, "refresh-token");
            rttr.addAttribute("alertCode", "MP201");
            return "redirect:/";
        } catch (Exception e) {
            rttr.addAttribute("alertCode", "C002");
            return "redirect:/mypage";
        }
    }

    private void loadMyInfoData(Model model) {
        try {
            var myInfo = memberService.getMyInfo().getBody();
            model.addAttribute("myInfo", myInfo);
        } catch (Exception e) {}
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}