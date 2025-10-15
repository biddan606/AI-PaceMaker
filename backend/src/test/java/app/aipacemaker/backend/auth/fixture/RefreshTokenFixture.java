package app.aipacemaker.backend.auth.fixture;

import app.aipacemaker.backend.auth.model.RefreshToken;

import java.time.LocalDateTime;

/**
 * RefreshToken 엔티티 테스트 픽스처
 */
public class RefreshTokenFixture {

    public static RefreshToken.RefreshTokenBuilder defaultRefreshToken(Long userId, String deviceId, String token) {
        return RefreshToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(30));
    }

    public static RefreshToken.RefreshTokenBuilder expiredRefreshToken(Long userId, String deviceId, String token) {
        return RefreshToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .token(token)
                .expiresAt(LocalDateTime.now().minusDays(1));
    }

    public static RefreshToken.RefreshTokenBuilder withExpiresAt(Long userId, String deviceId, String token, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .token(token)
                .expiresAt(expiresAt);
    }
}
