package com.example.chat.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void refresh(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
}
