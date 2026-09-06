package app.auth.service;

import app.auth.dto.IssuedRefreshToken;
import app.auth.model.RefreshToken;
import app.auth.repository.RefreshTokenRepository;
import app.common.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public IssuedRefreshToken issue(UUID userId) {
        return persistNewToken(userId);
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawToken) {
        RefreshToken existingRefreshToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (existingRefreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(existingRefreshToken.getUserId());
            throw new InvalidRefreshTokenException();
        }
        if (existingRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }

        existingRefreshToken.setRevoked(true);
        refreshTokenRepository.save(existingRefreshToken);

        return persistNewToken(existingRefreshToken.getUserId());
    }

    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private IssuedRefreshToken persistNewToken(UUID userId) {
        String rawToken = generateRawToken();
        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime expiresAt = createdOn.plus(Duration.ofMillis(refreshExpirationMs));

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(rawToken))
                .expiresAt(expiresAt)
                .revoked(false)
                .createdOn(createdOn)
                .build();
        refreshTokenRepository.save(refreshToken);

        return IssuedRefreshToken.builder()
                .userId(userId)
                .token(rawToken)
                .expiresAt(expiresAt)
                .build();
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available.", e);
        }
    }
}
