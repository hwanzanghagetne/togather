package com.example.chat.domain.user.repository;

import com.example.chat.domain.user.domain.RefreshToken;
import com.example.chat.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
}
