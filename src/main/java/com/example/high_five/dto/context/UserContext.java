package com.example.high_five.dto.context;

public record UserContext(Long id, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}