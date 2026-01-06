package com.example.high_five.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaviconController {

    @GetMapping("/favicon.ico")
    public String favicon() {
        // 브라우저가 /favicon.ico를 요청하면 /img/favicon.png 정적 리소스로 연결
        return "forward:/img/favicon.png";
    }
}