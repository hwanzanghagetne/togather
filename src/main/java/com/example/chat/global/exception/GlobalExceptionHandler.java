package com.example.chat.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 직접 던지는 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(Map.of(
                        "status", errorCode.getStatus(),
                        "message", errorCode.getMessage()
                ));
    }

    // @Valid 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다");

        return ResponseEntity
                .status(400)
                .body(Map.of(
                        "status", 400,
                        "message", message
                ));
    }

    // 그 외 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "status", 500,
                        "message", "서버 오류가 발생했습니다"
                ));
    }
}
