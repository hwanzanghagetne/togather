package com.example.chat.global.oauth2;

import com.example.chat.domain.user.domain.RefreshToken;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.repository.RefreshTokenRepository;
import com.example.chat.domain.user.service.CustomOAuth2User;
import com.example.chat.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        saveRefreshToken(user, refreshToken);

        response.addCookie(createCookie("access_token", accessToken, 60 * 30));
        response.addCookie(createCookie("refresh_token", refreshToken, 60 * 60 * 24 * 7));

        getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000");
    }

    private void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.create(user, refreshToken))
                );
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
