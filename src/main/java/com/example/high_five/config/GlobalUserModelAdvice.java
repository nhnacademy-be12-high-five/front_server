package com.example.high_five.config;

import com.example.high_five.dto.book.CategoryResponse;
import com.example.high_five.dto.context.UserContext;
import com.example.high_five.service.CategoryFeignClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalUserModelAdvice {


    @ModelAttribute("currentUser")
    public UserContext currentUser(HttpServletRequest request) {
        return (UserContext) request.getAttribute("user");
    }
}
