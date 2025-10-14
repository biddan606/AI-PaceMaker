package app.aipacemaker.backend.auth.model;

import java.util.Optional;

public interface EmailVerificationTokenRepository {
    EmailVerificationToken save(EmailVerificationToken token);
    Optional<EmailVerificationToken> findById(Long id);
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
