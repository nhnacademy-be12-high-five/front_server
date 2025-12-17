package com.example.high_five.config;

import com.example.high_five.dto.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserModelAdvice {

    @ModelAttribute("currentUser")
    public UserContext currentUser(HttpServletRequest request) {
        return (UserContext) request.getAttribute("user");
    }
}
