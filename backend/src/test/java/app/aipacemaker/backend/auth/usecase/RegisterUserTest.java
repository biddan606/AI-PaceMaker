package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.BaseIntegrationTest;
import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.DuplicateEmailException;
import app.aipacemaker.backend.auth.model.exception.InvalidPasswordException;
import app.aipacemaker.backend.config.mail.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

/**
 * 회원가입 Event-Driven 통합 테스트
 *
 * 테스트 범위:
 * RegisterUser UseCase → UserRegisteredEvent 발행 → UserRegisteredEventListener 처리 → 인증 이메일 발송
 *
 * EmailSender는 외부 서비스(서드파티)이므로 @MockBean으로 격리
 * 내부 협력 과정(UseCase → Event → Listener)은 실제 동작으로 검증
 */
@DisplayName("회원가입 통합 테스트")
class RegisterUserTest extends BaseIntegrationTest {

    @Autowired
    private RegisterUser registerUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @MockBean
    private EmailSender emailSender;

    @Test
    @DisplayName("사용자가 회원가입하면 계정이 생성되고 비밀번호가 암호화되어 저장되며 인증 토큰이 발급되고 인증 이메일이 발송된다")
    void successfulRegistration() {
        // given: 유효한 이메일과 비밀번호
        String email = "newuser@example.com";
        String rawPassword = "password123";
        RegisterUser.Command command = new RegisterUser.Command(email, rawPassword);

        // when: 회원가입 요청
        RegisterUser.Result result = registerUser.execute(command);

        // then: UseCase 실행 결과 검증
        assertThat(result.userId()).isNotNull();
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.verificationTokenCreated()).isTrue();

        // then: 사용자 계정이 생성되고 비밀번호가 암호화되어 저장됨
        Optional<User> savedUser = userRepository.findByEmail(email);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().isEmailVerified()).isFalse();
        assertThat(savedUser.get().getPassword())
                .isNotEqualTo(rawPassword)
                .startsWith("$2a$"); // BCrypt 암호화 확인

        // then: 인증 토큰이 생성됨
        Optional<EmailVerificationToken> token = tokenRepository.findById(result.userId());
        assertThat(token).isPresent();

        // then: 인증 이메일이 발송됨 (Event-Driven Flow의 최종 결과)
        // EmailSender는 외부 서비스이므로 Mock으로 검증
        verify(emailSender).sendEmail(
                anyString(),
                contains("이메일 인증"),
                anyString()
        );
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이거나 영문자와 숫자를 포함하지 않으면 InvalidPasswordException이 발생한다")
    void invalidPasswordFails() {
        // given: 정책 위반 비밀번호 (7자, 숫자 포함)
        RegisterUser.Command command = new RegisterUser.Command("test@example.com", "short1");

        // when & then: InvalidPasswordException 발생
        assertThatThrownBy(() -> registerUser.execute(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입하면 DuplicateEmailException이 발생한다")
    void duplicateEmailFails() {
        // given: 이미 존재하는 사용자
        User existingUser = User.builder()
                .email("existing@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        userRepository.save(existingUser);

        // given: 동일한 이메일로 회원가입 시도
        RegisterUser.Command command = new RegisterUser.Command("existing@example.com", "password123");

        // when & then: DuplicateEmailException 발생
        assertThatThrownBy(() -> registerUser.execute(command))
                .isInstanceOf(DuplicateEmailException.class);
    }
}
