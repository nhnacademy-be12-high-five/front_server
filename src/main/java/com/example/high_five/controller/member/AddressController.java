package com.example.high_five.controller.member;

import com.example.high_five.common.annotation.LoginRequired;
import com.example.high_five.dto.member.request.AddressRequest;
import com.example.high_five.dto.member.response.AddressListResponse;
import com.example.high_five.dto.member.response.AddressResponse;
import com.example.high_five.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/address")
public class AddressController {

    private final AddressService addressService;

    // 1. 목록 조회
    // 1. 목록 조회
    @GetMapping
    @LoginRequired
    public String addressList(Model model) {
        try {
            // 백엔드 응답 가져오기
            AddressListResponse response = addressService.getAddressList().getBody();

            List<AddressResponse> list = (response != null && response.getAddressList() != null)
                    ? response.getAddressList()
                    : Collections.emptyList();

            model.addAttribute("addresses", list);
        } catch (Exception e) {
            log.error("배송지 목록 조회 실패", e);
            model.addAttribute("addresses", Collections.emptyList());
        }
        model.addAttribute("currentTab", "address");
        return "mypage/address";
    }

    // 2. 등록 폼
    @GetMapping("/register")
    @LoginRequired
    public String registerForm(Model model) {
        model.addAttribute("address", new AddressRequest());
        model.addAttribute("isUpdate", false);
        return "mypage/address-form";
    }

    // 3. 등록 처리
    @PostMapping("/register")
    @LoginRequired
    public String registerAddress(@ModelAttribute AddressRequest request, RedirectAttributes rttr) {
        try {
            addressService.createAddress(request);
            rttr.addFlashAttribute("message", "배송지가 추가되었습니다.");
        } catch (Exception e) {
            log.error("등록 실패", e);
            rttr.addFlashAttribute("errorMessage", "등록 실패: " + e.getMessage());
        }
        return "redirect:/mypage/address";
    }

    // 4. 수정 폼 (기존 데이터 조회)
    @GetMapping("/{id}/update")
    @LoginRequired
    public String updateForm(@PathVariable Long id, Model model) {
        try {
            AddressResponse response = addressService.getAddress(id).getBody();
            model.addAttribute("address", response);
            model.addAttribute("isUpdate", true);
            model.addAttribute("addressId", id);
            return "mypage/address-form";
        } catch (Exception e) {
            return "redirect:/mypage/address";
        }
    }

    // 5. 수정 처리 (PATCH)
    @PostMapping("/{id}/update")
    @LoginRequired
    public String updateAddress(@PathVariable Long id,
                                @ModelAttribute @Valid AddressRequest request,
                                RedirectAttributes rttr) {
        try {
            log.info("프론트엔드 컨트롤러에 들어온 값: {}", request.isDefaultAddress());
            addressService.updateAddress(id, request);
            rttr.addFlashAttribute("message", "배송지가 수정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "수정 실패");
        }
        return "redirect:/mypage/address";
    }

    // 6. 삭제 처리
    @PostMapping("/{id}/delete")
    @LoginRequired
    public String deleteAddress(@PathVariable Long id, RedirectAttributes rttr) {
        try {
            addressService.deleteAddress(id);
            rttr.addFlashAttribute("message", "삭제되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "삭제 실패");
        }
        return "redirect:/mypage/address";
    }

    // 7. [추가] 기본 배송지 설정
    @PostMapping("/{id}/default")
    @LoginRequired
    public String setDefaultAddress(@PathVariable Long id, RedirectAttributes rttr) {
        try {
            addressService.setDefaultAddress(id);
            rttr.addFlashAttribute("message", "기본 배송지로 설정되었습니다.");
        } catch (Exception e) {
            rttr.addFlashAttribute("errorMessage", "설정 실패");
        }
        return "redirect:/mypage/address";
    }
}