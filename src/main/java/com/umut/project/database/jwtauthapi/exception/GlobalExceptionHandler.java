package com.umut.project.database.jwtauthapi.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleBaseException(BaseException exception, WebRequest request) {
        Map<String, Object> response = Map.of(
                "timestamp", new Date(),
                "status", exception.getStatus(),
                "error", exception.getStatus().getReasonPhrase(),
                "message", exception.getMessage(),
                "path", request.getDescription(false),
                "errorCode", exception.getStatus().value()
        );

        return ResponseEntity.status(exception.getStatus()).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException exception, WebRequest request) {
        Map<String, Object> response = Map.of(
                "timestamp", new Date(),
                "status", HttpStatus.UNAUTHORIZED,
                "error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "message", "Kullanıcı adı veya parola hatalı!",
                "path", request.getDescription(false),
                "errorCode", HttpStatus.UNAUTHORIZED.value()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtException(JwtException exception, WebRequest request) {
        Map<String, Object> response = Map.of(
                "timestamp", new Date(),
                "status", HttpStatus.UNAUTHORIZED,
                "error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "message", "Token süresi dolmuş veya geçersiz!",
                "path", request.getDescription(false),
                "errorCode", HttpStatus.UNAUTHORIZED.value()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        Map<String, Object> response = Map.of(
                "timestamp", new Date(),
                "status", HttpStatus.FORBIDDEN,
                "error", HttpStatus.FORBIDDEN.getReasonPhrase(),
                "message", "Bu işlem için yetkiniz bulunmamaktadır!",
                "path", request.getDescription(false),
                "errorCode", HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            WebRequest request
    ) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", new Date(),
                "status", HttpStatus.BAD_REQUEST,
                "error", HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "path", request.getDescription(false),
                "errorCode", HttpStatus.BAD_REQUEST.value(),
                "errors", fieldErrors
        ));
    }
}
