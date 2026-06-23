package com.example.chat.domain.user.service;

import com.example.chat.domain.user.dto.UserResponse;

public interface UserService {
    UserResponse getMe(Long userId);
}
