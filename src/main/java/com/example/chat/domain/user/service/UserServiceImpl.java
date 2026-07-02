package com.example.chat.domain.user.service;

import com.example.chat.domain.meetup.repository.MeetupParticipantRepository;
import com.example.chat.domain.meetup.repository.MeetupRepository;
import com.example.chat.domain.meetup.repository.MeetupReviewRepository;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.dto.UserResponse;
import com.example.chat.domain.user.dto.UserStatsResponse;
import com.example.chat.domain.user.dto.UserUpdateRequest;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MeetupRepository meetupRepository;
    private final MeetupParticipantRepository meetupParticipantRepository;
    private final MeetupReviewRepository meetupReviewRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserResponse updateMe(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.nickname(), request.bio(), request.profileImageUrl());
        return UserResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getStats(Long userId) {
        long joinedCount = meetupParticipantRepository.countByUserId(userId);
        long hostedCount = meetupRepository.countByHostId(userId);
        long reviewCount = meetupReviewRepository.countByReviewerId(userId);
        return new UserStatsResponse(joinedCount, hostedCount, reviewCount, 36.5);
    }
}
