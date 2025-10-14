package app.aipacemaker.backend.auth.adapter;

import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationToken, Long>, EmailVerificationTokenRepository {
}
