package com.example.high_five.controller.member;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AddressController {

    @GetMapping("/address")
    public String addressList(Model model) {


        // HTML로 데이터 전달
//        model.addAttribute("addressList", addressList);

        // return 값은 실제 HTML 파일의 경로와 이름이어야 함 (예: resources/templates/mypage/address.html)
        return "mypage/address";
    }

//    // 2. 배송지 추가 처리 (POST)
//    // HTML의 <form action="/mypage/address/add">와 매칭됨
//    @PostMapping("/address/add")
//    public String addAddress(@ModelAttribute AddressRequestDto request) {
//        return "redirect:/mypage/address"; // 처리가 끝나면 다시 목록으로 이동
//    }
}
