package com.example.chat.domain.user.service;

import com.example.chat.domain.user.dto.UserResponse;
import com.example.chat.domain.user.dto.UserStatsResponse;
import com.example.chat.domain.user.dto.UserUpdateRequest;

public interface UserService {
    UserResponse getMe(Long userId);
    UserResponse updateMe(Long userId, UserUpdateRequest request);
    UserStatsResponse getStats(Long userId);
}
