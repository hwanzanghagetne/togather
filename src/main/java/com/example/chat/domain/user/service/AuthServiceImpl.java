package com.example.chat.domain.user.service;

import com.example.chat.domain.user.domain.RefreshToken;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.RefreshTokenRepository;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import com.example.chat.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        jwtUtil.validate(refreshToken);

        Long userId = jwtUtil.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        RefreshToken saved = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!saved.getToken().equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtUtil.createAccessToken(user.getId(), user.getRole().name());
        response.addCookie(createCookie("access_token", newAccessToken, 60 * 30));
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");

        if (refreshToken != null) {
            Long userId = jwtUtil.getUserId(refreshToken);
            userRepository.findById(userId).ifPresent(user ->
                    refreshTokenRepository.findByUser(user)
                            .ifPresent(refreshTokenRepository::delete)
            );
        }

        response.addCookie(expireCookie("access_token"));
        response.addCookie(expireCookie("refresh_token"));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private Cookie expireCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
