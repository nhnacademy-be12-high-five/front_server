package com.example.high_five.controller.admin;

import com.example.high_five.dto.book.BookPagedResponse;
import com.example.high_five.dto.book.response.BookResponse;
import com.example.high_five.dto.book.CategoryResponse;
import com.example.high_five.dto.coupon.*;
import com.example.high_five.service.BookFeignClient;
import com.example.high_five.service.CategoryFeignClient;
import com.example.high_five.service.CouponService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CouponAdminController {
    private final CouponService couponService;
    private final BookFeignClient bookFeignClient;
    private final CategoryFeignClient categoryFeignClient;

    @GetMapping("/admin/coupons/page")
    public String couponPage(@CookieValue(value = "access-token", required = false) String accessToken,
                             Model model){

        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/member/login.html";
        }

        try {
            List<CouponTemplateDto> coupons = couponService.getAdminCoupons();
            List<CouponPolicyResponseDto> policies = couponService.getAllPolicies();

            model.addAttribute("coupons", coupons);
            model.addAttribute("policies", policies);
            return "admin/coupons";

        } catch (FeignException.Unauthorized e) {
            System.out.println("인증 실패(401): 토큰이 유효하지 않습니다. 로그인 페이지로 이동합니다.");
            return "redirect:/member/login.html";
        } catch (FeignException.Forbidden e) {
            return "redirect:/";
        } catch (FeignException e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/admin/coupons/policies/create")
    public String createPolicy(@ModelAttribute CouponPolicyRequestDto dto,
                               RedirectAttributes redirectAttributes) {

        if (dto.getTargetBookIds() == null) {
            dto.setTargetBookIds(new ArrayList<>());
        }
        if (dto.getTargetCategoryIds() == null) {
            dto.setTargetCategoryIds(new ArrayList<>());
        }

        try {
            couponService.createCouponPolicy(dto);
            redirectAttributes.addFlashAttribute("message", "쿠폰 정책이 성공적으로 생성되었습니다.");
        } catch (FeignException e) {
            String serverMessage = e.getMessage();
            String alertMsg = "정책 생성 실패: " + e.status();

            if (serverMessage != null && serverMessage.contains("\"message\":\"")) {
                int start = serverMessage.indexOf("\"message\":\"") + 11;
                int end = serverMessage.indexOf("\"", start);
                if (end > start) {
                    alertMsg = serverMessage.substring(start, end);
                }
            }

            redirectAttributes.addFlashAttribute("errorMessage", alertMsg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            e.printStackTrace();
        }

        return "redirect:/admin/coupons/page";
    }

    @PostMapping("/admin/coupons/create")
    public String createCouponTemplate(@ModelAttribute CouponCreateRequestDto dto, RedirectAttributes redirectAttributes) {
        try {
            couponService.createCouponTemplate(dto);
            redirectAttributes.addFlashAttribute("message", "쿠폰 템플릿이 성공적으로 생성되었습니다.");
        } catch (FeignException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "템플릿 생성 실패: 정책이 비활성화 상태이거나 잘못된 요청입니다.");
        }
        return "redirect:/admin/coupons/page";
    }

    @PostMapping("/admin/coupons/policies/{id}")
    public String disablePolicy(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            couponService.disableCouponPolicy(id);
            redirectAttributes.addFlashAttribute("message", "정책이 비활성화되었으며, 관련 쿠폰이 모두 만료 처리되었습니다.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "정책 비활성화 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/coupons/page";
    }

    @PostMapping("/admin/coupons/member-coupons/issue")
    public String issueCouponManually(@RequestParam Long userId,
                                      @RequestParam Long couponId,
                                      RedirectAttributes redirectAttributes) {
        try {
            MemberCouponIssueRequestDto requestDto = new MemberCouponIssueRequestDto(userId, couponId);
            couponService.issueCouponByAdmin(requestDto);

            redirectAttributes.addFlashAttribute("message", "회원(" + userId + ")에게 쿠폰이 정상적으로 지급되었습니다.");
        } catch (FeignException e) {
            String serverMessage = e.contentUTF8();

            if (e.status() == 400 && serverMessage != null) {
                redirectAttributes.addFlashAttribute("errorMessage", serverMessage);
            } else if (e.status() == 409) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 해당 쿠폰을 보유한 회원입니다.");
            } else if (e.status() == 404) {
                redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 회원 또는 쿠폰입니다.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "쿠폰 지급 실패 (오류: " + e.status() + ")");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            e.printStackTrace();
        }

        return "redirect:/admin/coupons/page";
    }

    @GetMapping("/admin/coupons/policies/{id}")
    public String policyDetail(@PathVariable("id") Long id, Model model) {
        // 1. Feign Client로 백엔드 데이터 조회
        CouponPolicyResponseDto policy = couponService.getCouponPolicy(id);

        // 2. 모델에 담기
        model.addAttribute("policy", policy);

        // 3. 상세 페이지 뷰 반환 (새로 만들 파일)
        return "admin/policy-detail";
    }

    @GetMapping("/admin/coupons/books/search")
    @ResponseBody // JSON 데이터를 반환하기 위해 사용
    public ResponseEntity<List<BookResponse>> searchBooksForCoupon(@RequestParam("keyword") String keyword) {
        try {
            // Book Server 검색 API 호출 (첫 페이지, 10개만 조회)
            BookPagedResponse<BookResponse> response = bookFeignClient.searchBooks(keyword, 0, 10);

            if (response != null && response.getContent() != null) {
                return ResponseEntity.ok(response.getContent());
            } else {
                return ResponseEntity.ok(Collections.emptyList());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 에러 발생 시 빈 리스트 반환 혹은 에러 처리
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * [추가] 1차 카테고리 목록 조회 (AJAX용)
     */
    @GetMapping("/admin/coupons/categories/parent")
    @ResponseBody
    public ResponseEntity<List<CategoryResponse>> getParentCategories() {
        return ResponseEntity.ok(categoryFeignClient.getParentCategories());
    }

    /**
     * [추가] 2차 카테고리 목록 조회 (AJAX용)
     */
    @GetMapping("/admin/coupons/categories/{parentId}/child")
    @ResponseBody
    public ResponseEntity<List<CategoryResponse>> getChildCategories(@PathVariable int parentId) {
        return ResponseEntity.ok(categoryFeignClient.getChildCategories(parentId));
    }

    @GetMapping("/admin/coupons/specific-book-coupons")
    public String specificBookCouponPage(Model model) {
        List<CouponPolicyResponseDto> policies = couponService.getAllPolicies();
        model.addAttribute("policies", policies);
        return "admin/book-coupons";
    }
}