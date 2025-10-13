package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.TestcontainersConfiguration;
import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.DuplicateEmailException;
import app.aipacemaker.backend.auth.model.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("RegisterUser Use Case 통합 테스트")
class RegisterUserTest {

    @Autowired
    private RegisterUser registerUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Test
    @DisplayName("유효한 요청으로 사용자를 생성하고 인증 토큰을 발급한다")
    void registerUserSuccessfully() {
        // given
        RegisterUser.Command command = new RegisterUser.Command("test@example.com", "password123");

        // when
        RegisterUser.Result result = registerUser.execute(command);

        // then
        assertThat(result.userId()).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.verificationTokenCreated()).isTrue();

        // 사용자가 DB에 저장되었는지 확인
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().isEmailVerified()).isFalse();

        // 인증 토큰이 생성되었는지 확인
        Optional<EmailVerificationToken> token = tokenRepository.findById(result.userId());
        assertThat(token).isPresent();
    }

    @Test
    @DisplayName("비밀번호 정책 위반 시 예외가 발생한다")
    void throwsExceptionWhenPasswordInvalid() {
        // given
        RegisterUser.Command command = new RegisterUser.Command("test@example.com", "short1");

        // when & then
        assertThatThrownBy(() -> registerUser.execute(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("중복 이메일로 가입 시도 시 예외가 발생한다")
    void throwsExceptionWhenEmailDuplicated() {
        // given
        User existingUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        userRepository.save(existingUser);

        RegisterUser.Command command = new RegisterUser.Command("test@example.com", "password123");

        // when & then
        assertThatThrownBy(() -> registerUser.execute(command))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @DisplayName("비밀번호는 암호화되어 저장된다")
    void passwordIsEncoded() {
        // given
        String rawPassword = "password123";
        RegisterUser.Command command = new RegisterUser.Command("test@example.com", rawPassword);

        // when
        RegisterUser.Result result = registerUser.execute(command);

        // then
        Optional<User> savedUser = userRepository.findById(result.userId());
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).isNotEqualTo(rawPassword);
    }
}
