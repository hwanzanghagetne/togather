package com.example.chat.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(400, "잘못된 입력입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "권한이 없습니다"),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다"),

    // 토큰
    TOKEN_EXPIRED(401, "토큰이 만료됐습니다"),
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다"),

    // 회원
    USER_NOT_FOUND(404, "존재하지 않는 회원입니다"),
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다"),

    // 모임
    MEETUP_NOT_FOUND(404, "존재하지 않는 모임입니다"),
    MEETUP_EXPIRED(400, "만료된 모임입니다"),
    MEETUP_NOT_JOINABLE(400, "참가할 수 없는 모임입니다"),
    ALREADY_JOINED(409, "이미 참가한 모임입니다"),
    DUPLICATE_REVIEW(409, "이미 후기를 작성했습니다");

    private final int status;
    private final String message;
}
