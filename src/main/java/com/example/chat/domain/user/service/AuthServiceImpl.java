package com.example.chat.domain.user.service;

import com.example.chat.domain.user.domain.RefreshToken;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.RefreshTokenRepository;
import com.example.chat.domain.user.repository.UserRepository;
import com.example.chat.global.exception.BusinessException;
import com.example.chat.global.exception.ErrorCode;
import com.example.chat.global.jwt.JwtUtil;
import com.example.chat.global.oauth2.AuthCodeStore;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthCodeStore authCodeStore;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Override
    @Transactional
    public void exchange(String code, HttpServletResponse response) {
        Long userId = authCodeStore.consume(code);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, refreshToken))
                );

        response.addCookie(createCookie("access_token", accessToken, 60 * 30));
        response.addCookie(createCookie("refresh_token", refreshToken, 60 * 60 * 24 * 7));
    }

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
            try {
                Long userId = jwtUtil.getUserId(refreshToken);
                userRepository.findById(userId).ifPresent(user ->
                        refreshTokenRepository.findByUser(user)
                                .ifPresent(refreshTokenRepository::delete)
                );
            } catch (BusinessException ignored) {
                // 만료되거나 손상된 refresh_token 이어도 로그아웃은 쿠키/세션 정리까지 수행한다.
            }
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.addCookie(expireCookie("access_token"));
        response.addCookie(expireCookie("refresh_token"));
        response.addCookie(expireCookie("JSESSIONID"));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", sameSite);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private Cookie expireCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", sameSite);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }
}
