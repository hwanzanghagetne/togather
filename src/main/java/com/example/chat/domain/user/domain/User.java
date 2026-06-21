package com.example.chat.domain.user.domain;

import com.example.chat.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    private String homeCity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    private User(String email, String password, String nickname, String profileImageUrl, String homeCity, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.homeCity = homeCity;
        this.role = role;
    }

    public static User create(String email, String password, String nickname, String homeCity) {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .homeCity(homeCity)
                .role(Role.USER)
                .build();
    }
}
