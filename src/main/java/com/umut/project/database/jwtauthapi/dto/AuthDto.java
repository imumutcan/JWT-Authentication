package com.umut.project.database.jwtauthapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AuthDto {

    public record LoginRequest(
            @NotBlank(message = "Username cannot be null!")
            String username,
            @NotBlank(message = "Password cannot be null!")
            String password
    ) {}

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 8, max = 50, message = "Please enter a valid username")
            String username,
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
            String password,
            @NotBlank(message = "Email is required")
            @Email(message = "Please enter a valid email")
            String email
    ) {}

    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token cannot be null!")
            @JsonAlias("refreshtoken")
            String refreshToken
    ) {}

    public record LoginResponse(
            String refreshToken,
            String accessToken,
            String username,
            String message
    ) {}

    public record RefreshTokenResponse(
            String refreshToken,
            String accessToken,
            String tokenType
    ) {
        public RefreshTokenResponse(String refreshToken, String accessToken) {
            this(refreshToken, accessToken, "Bearer");
        }
    }

    public record RegisterResponse(
            String username,
            Long id
    ) {}

    public record UserInfoResponse(
            Long id,
            String username,
            String email,
            List<String> roles
    ) {}
}
