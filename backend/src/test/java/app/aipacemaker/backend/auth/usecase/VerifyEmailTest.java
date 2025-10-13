package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.BaseIntegrationTest;
import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.ExpiredVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidVerificationTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이메일 인증 통합 테스트
 */
@DisplayName("이메일 인증 통합 테스트")
class VerifyEmailTest extends BaseIntegrationTest {

    @Autowired
    private VerifyEmail verifyEmail;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Test
    @DisplayName("사용자가 유효한 인증 토큰으로 이메일 인증을 완료하면 계정이 활성화되고 토큰이 삭제된다")
    void successfulVerification() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(savedUser.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        VerifyEmail.Command command = new VerifyEmail.Command(token);

        // when
        VerifyEmail.Result result = verifyEmail.execute(command);

        // then
        assertThat(result.userId()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.verified()).isTrue();

        // 사용자가 인증되었는지 확인
        Optional<User> verifiedUser = userRepository.findById(savedUser.getId());
        assertThat(verifiedUser).isPresent();
        assertThat(verifiedUser.get().isEmailVerified()).isTrue();

        // 토큰이 삭제되었는지 확인
        Optional<EmailVerificationToken> deletedToken = tokenRepository.findByToken(token);
        assertThat(deletedToken).isEmpty();
    }

    @Test
    @DisplayName("만료된 인증 토큰으로 이메일 인증을 시도하면 ExpiredVerificationTokenException이 발생한다")
    void expiredTokenFails() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken expiredToken = EmailVerificationToken.builder()
                .userId(savedUser.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        tokenRepository.save(expiredToken);

        VerifyEmail.Command command = new VerifyEmail.Command(token);

        // when & then
        assertThatThrownBy(() -> verifyEmail.execute(command))
                .isInstanceOf(ExpiredVerificationTokenException.class);
    }

    @Test
    @DisplayName("존재하지 않는 인증 토큰으로 이메일 인증을 시도하면 InvalidVerificationTokenException이 발생한다")
    void invalidTokenFails() {
        // given
        VerifyEmail.Command command = new VerifyEmail.Command("nonexistent-token");

        // when & then
        assertThatThrownBy(() -> verifyEmail.execute(command))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    @DisplayName("이미 인증이 완료된 사용자가 다시 인증 토큰으로 인증하면 성공한다")
    void alreadyVerifiedUserSuccess() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .userId(savedUser.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        VerifyEmail.Command command = new VerifyEmail.Command(token);

        // when
        VerifyEmail.Result result = verifyEmail.execute(command);

        // then
        assertThat(result.verified()).isTrue();
    }
}
