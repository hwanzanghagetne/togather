package com.example.chat.domain.user.service;

import com.example.chat.domain.user.domain.OAuthProvider;
import com.example.chat.domain.user.domain.User;
import com.example.chat.domain.user.domain.UserOAuth;
import com.example.chat.domain.user.repository.UserOAuthRepository;
import com.example.chat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserOAuthRepository userOAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        String providerUserId = oAuth2User.getName(); // 구글 고유 ID
        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("name");
        String profileImageUrl = oAuth2User.getAttribute("picture");

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
