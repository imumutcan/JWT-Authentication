package com.umut.project.database.jwtauthapi.service;

import com.umut.project.database.jwtauthapi.entity.RefreshToken;
import com.umut.project.database.jwtauthapi.entity.User;
import com.umut.project.database.jwtauthapi.exception.InvalidTokenException;
import com.umut.project.database.jwtauthapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser_Id(user.getId());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token süresi dolmuş, lütfen tekrar giriş yapınız!");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken refreshToken) {
        verifyExpiration(refreshToken);
        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        refreshTokenRepository.flush();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setToken(UUID.randomUUID().toString());
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(newRefreshToken);
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser_Id(userId);
    }

    @Transactional
    public int deleteByToken(String token) {
        return refreshTokenRepository.deleteByToken(token);
    }

    @Transactional
    @Scheduled(fixedRateString = "${jwt.refresh-token-expiration-ms}")
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredSince(Instant.now());
    }
}
