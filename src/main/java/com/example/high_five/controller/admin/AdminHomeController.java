package com.example.high_five.controller.admin;

import com.example.high_five.common.annotation.LoginRequired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    @GetMapping
    @LoginRequired(adminOnly = true)
    public String adminHome() {

        return "admin/admin-home";
    }
}