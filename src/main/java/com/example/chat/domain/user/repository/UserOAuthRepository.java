package com.example.chat.domain.user.repository;

import com.example.chat.domain.user.domain.OAuthProvider;
import com.example.chat.domain.user.domain.UserOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOAuthRepository extends JpaRepository<UserOAuth, Long> {
    Optional<UserOAuth> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
}
