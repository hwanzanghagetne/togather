package com.example.chat.domain.user.service;

import com.example.chat.domain.user.domain.OAuthProvider;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.domain.UserOAuth;
import com.example.chat.domain.user.repository.UserOAuthRepository;
import com.example.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserOAuthRepository userOAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2 attributes: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        String providerUserId;
        String email;
        String nickname;
        String profileImageUrl;

        if ("kakao".equals(registrationId)) {
            // 카카오: {"id": 123, "kakao_account": {...}, "properties": {"nickname": "..."}}}
            Long kakaoId = oAuth2User.getAttribute("id");
            providerUserId = String.valueOf(kakaoId);
            java.util.Map<String, Object> properties = oAuth2User.getAttribute("properties");
            nickname = properties != null ? (String) properties.get("nickname") : "카카오유저";
            // 이메일 권한 없으므로 카카오 ID 기반 임시 이메일 생성
            email = "kakao_" + providerUserId + "@togather.com";
            profileImageUrl = properties != null ? (String) properties.get("profile_image") : null;
        } else {
            // 구글
            providerUserId = oAuth2User.getName();
            email = oAuth2User.getAttribute("email");
            nickname = oAuth2User.getAttribute("name");
            profileImageUrl = oAuth2User.getAttribute("picture");
        }

        // 기존 소셜 연동 확인
        UserOAuth userOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElseGet(() -> registerNewUser(email, nickname, profileImageUrl, provider, providerUserId));

        return new CustomOAuth2User(userOAuth.getUser(), oAuth2User.getAttributes());
    }

    private UserOAuth registerNewUser(String email, String nickname, String profileImageUrl,
                                      OAuthProvider provider, String providerUserId) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.create(email, null, nickname, null)
                ));

        return userOAuthRepository.save(
                UserOAuth.create(user, provider, providerUserId)
        );
    }
}
