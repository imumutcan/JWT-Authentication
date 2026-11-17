package com.umut.project.database.jwtauthapi.service;

import com.umut.project.database.jwtauthapi.dto.AuthDto;
import com.umut.project.database.jwtauthapi.entity.RefreshToken;
import com.umut.project.database.jwtauthapi.entity.Role;
import com.umut.project.database.jwtauthapi.entity.User;
import com.umut.project.database.jwtauthapi.exception.EmailAlreadyExistsException;
import com.umut.project.database.jwtauthapi.exception.InvalidTokenException;
import com.umut.project.database.jwtauthapi.exception.UserAlreadyExistsException;
import com.umut.project.database.jwtauthapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Transactional
    public AuthDto.RegisterResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already exists with username : " + request.username());
        }
        if (userRepository.existsByMail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exists with email : " + request.email());
        }

        User user = toEntity(request);
        User savedUser = userRepository.save(user);

        return toRegisterResponse(savedUser);
    }

    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user);

        return toLoginResponse(refreshToken.getToken(), accessToken, user.getUsername());
    }

    @Transactional
    public AuthDto.RefreshTokenResponse refresh(AuthDto.RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::rotateRefreshToken)
                .map(rotatedToken -> {
                    User user = rotatedToken.getUser();
                    String newAccessToken = jwtService.generateToken(user);
                    return toRefreshTokenResponse(rotatedToken.getToken(), newAccessToken);
                })
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token!"));
    }

    @Transactional
    public void logout(AuthDto.RefreshTokenRequest request) {
        int deletedCount = refreshTokenService.deleteByToken(request.refreshToken());
        if (deletedCount == 0) {
            throw new InvalidTokenException("Invalid or expired token!");
        }
    }

    private User toEntity(AuthDto.RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        user.setMail(request.email());
        user.setRole(Role.USER.getValue());
        return user;
    }

    private AuthDto.RegisterResponse toRegisterResponse(User user) {
        return new AuthDto.RegisterResponse(user.getUsername(), user.getId());
    }

    private AuthDto.LoginResponse toLoginResponse(String refreshToken, String accessToken, String username) {
        return new AuthDto.LoginResponse(refreshToken, accessToken, username, "Login is successful!");
    }

    private AuthDto.RefreshTokenResponse toRefreshTokenResponse(String refreshToken, String accessToken) {
        return new AuthDto.RefreshTokenResponse(refreshToken, accessToken);
    }
}
