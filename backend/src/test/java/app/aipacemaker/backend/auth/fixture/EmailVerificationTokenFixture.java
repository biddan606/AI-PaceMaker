package app.aipacemaker.backend.auth.fixture;

import app.aipacemaker.backend.auth.model.EmailVerificationToken;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * EmailVerificationToken 엔티티 테스트 픽스처
 */
public class EmailVerificationTokenFixture {

    public static EmailVerificationToken.EmailVerificationTokenBuilder defaultToken(Long userId) {
        return EmailVerificationToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(24));
    }

    public static EmailVerificationToken.EmailVerificationTokenBuilder expiredToken(Long userId) {
        return EmailVerificationToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().minusHours(1));
    }

    public static EmailVerificationToken.EmailVerificationTokenBuilder withToken(Long userId, String token) {
        return EmailVerificationToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24));
    }

    public static EmailVerificationToken.EmailVerificationTokenBuilder withExpiresAt(Long userId, String token, LocalDateTime expiresAt) {
        return EmailVerificationToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt);
    }
}
