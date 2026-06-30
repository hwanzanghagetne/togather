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

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserOAuthRepository userOAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());
        OAuthProfile profile = extractProfile(registrationId, oAuth2User);

        UserOAuth userOAuth = userOAuthRepository
                .findByProviderAndProviderUserId(provider, profile.providerUserId())
                .orElseGet(() -> connectOAuthAccount(profile, provider));

        return new CustomOAuth2User(userOAuth.getUser(), oAuth2User.getAttributes());
    }

    private UserOAuth connectOAuthAccount(OAuthProfile profile, OAuthProvider provider) {
        User user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> userRepository.save(
                        User.create(profile.email(), null, profile.nickname(), null)
                ));

        return userOAuthRepository.save(
                UserOAuth.create(user, provider, profile.providerUserId())
        );
    }

    private OAuthProfile extractProfile(String registrationId, OAuth2User oAuth2User) {
        if ("kakao".equals(registrationId)) {
            return extractKakaoProfile(oAuth2User);
        }
        return extractGoogleProfile(oAuth2User);
    }

    private OAuthProfile extractKakaoProfile(OAuth2User oAuth2User) {
        Long kakaoId = oAuth2User.getAttribute("id");
        if (kakaoId == null) {
            throw new OAuth2AuthenticationException("kakao id not found");
        }

        Map<String, Object> kakaoAccount = getMap(oAuth2User.getAttribute("kakao_account"));
        String email = getRequiredEmail(kakaoAccount != null ? kakaoAccount.get("email") : null, "kakao");

        Map<String, Object> properties = getMap(oAuth2User.getAttribute("properties"));
        String nickname = Optional.ofNullable(properties)
                .map(map -> (String) map.get("nickname"))
                .filter(value -> !value.isBlank())
                .orElse("카카오유저");
        String profileImageUrl = properties != null ? (String) properties.get("profile_image") : null;

        return new OAuthProfile(String.valueOf(kakaoId), email, nickname, profileImageUrl);
    }

    private OAuthProfile extractGoogleProfile(OAuth2User oAuth2User) {
        String providerUserId = oAuth2User.getName();
        String email = getRequiredEmail(oAuth2User.getAttribute("email"), "google");
        String nickname = Optional.ofNullable((String) oAuth2User.getAttribute("name"))
                .filter(value -> !value.isBlank())
                .orElse("구글유저");
        String profileImageUrl = oAuth2User.getAttribute("picture");

        return new OAuthProfile(providerUserId, email, nickname, profileImageUrl);
    }

    private Map<String, Object> getMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) map;
            return casted;
        }
        return null;
    }

    private String getRequiredEmail(Object emailValue, String provider) {
        if (emailValue instanceof String email && !email.isBlank()) {
            return email;
        }
        throw new OAuth2AuthenticationException(provider + " email consent required");
    }

    private record OAuthProfile(
            String providerUserId,
            String email,
            String nickname,
            String profileImageUrl
    ) {
    }
}
