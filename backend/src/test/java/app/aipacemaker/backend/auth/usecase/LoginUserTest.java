package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.BaseIntegrationTest;
import app.aipacemaker.backend.auth.infrastructure.JwtTokenProvider;
import app.aipacemaker.backend.auth.model.RefreshToken;
import app.aipacemaker.backend.auth.model.RefreshTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 로그인 Event-Driven 통합 테스트
 *
 * 테스트 범위:
 * LoginUser UseCase → JWT 토큰 발급 → Refresh Token DB 저장
 */
@DisplayName("로그인 통합 테스트")
class LoginUserTest extends BaseIntegrationTest {

    @Autowired
    private LoginUser loginUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("유효한 이메일과 비밀번호로 로그인하면 Access Token과 Refresh Token이 발급되고 Refresh Token이 DB에 저장된다")
    void successfulLogin() {
        // given: 이메일 인증된 사용자
        String email = "verified@example.com";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name("테스트유저")
                .email(email)
                .password(encodedPassword)
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId = "test-device-001";
        LoginUser.Command command = new LoginUser.Command(email, rawPassword, deviceId);

        // when: 로그인 요청
        LoginUser.Result result = loginUser.execute(command);

        // then: UseCase 실행 결과 검증
        assertThat(result.userId()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.emailVerified()).isTrue();
        assertThat(result.accessToken()).isNotNull().isNotEmpty();
        assertThat(result.refreshToken()).isNotNull().isNotEmpty();

        // then: Access Token 검증
        assertThat(jwtTokenProvider.validateToken(result.accessToken())).isTrue();
        assertThat(jwtTokenProvider.extractUserId(result.accessToken())).isEqualTo(savedUser.getId());
        assertThat(jwtTokenProvider.extractEmail(result.accessToken())).isEqualTo(email);

        // then: Refresh Token이 해당 디바이스로 DB에 저장됨
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findByUserIdAndDeviceId(savedUser.getId(), deviceId);
        assertThat(savedRefreshToken).isPresent();
        assertThat(savedRefreshToken.get().getToken()).isEqualTo(result.refreshToken());
        assertThat(savedRefreshToken.get().getDeviceId()).isEqualTo(deviceId);
        assertThat(savedRefreshToken.get().isExpired()).isFalse();
    }

    @Test
    @DisplayName("이메일 미인증 사용자가 로그인하면 토큰이 발급되고 emailVerified가 false로 반환된다")
    void loginWithUnverifiedEmail() {
        // given: 이메일 미인증 사용자
        String email = "unverified@example.com";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name("미인증유저")
                .email(email)
                .password(encodedPassword)
                .emailVerified(false)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId = "test-device-002";
        LoginUser.Command command = new LoginUser.Command(email, rawPassword, deviceId);

        // when: 로그인 요청
        LoginUser.Result result = loginUser.execute(command);

        // then: 로그인은 성공하지만 emailVerified가 false
        assertThat(result.userId()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.emailVerified()).isFalse();
        assertThat(result.accessToken()).isNotNull().isNotEmpty();
        assertThat(result.refreshToken()).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 InvalidCredentialsException이 발생한다")
    void loginWithWrongPassword() {
        // given: 이메일 인증된 사용자
        String email = "user@example.com";
        String correctPassword = "password123";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        User user = User.builder()
                .name("테스트유저")
                .email(email)
                .password(encodedPassword)
                .emailVerified(true)
                .build();
        userRepository.save(user);

        // given: 잘못된 비밀번호로 로그인 시도
        String deviceId = "test-device-003";
        LoginUser.Command command = new LoginUser.Command(email, "wrongPassword", deviceId);

        // when & then: InvalidCredentialsException 발생
        assertThatThrownBy(() -> loginUser.execute(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 InvalidCredentialsException이 발생한다")
    void loginWithNonExistentEmail() {
        // given: 존재하지 않는 이메일
        String deviceId = "test-device-004";
        LoginUser.Command command = new LoginUser.Command("nonexistent@example.com", "password123", deviceId);

        // when & then: InvalidCredentialsException 발생
        assertThatThrownBy(() -> loginUser.execute(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("동일 사용자가 같은 기기에서 다시 로그인하면 해당 기기의 Refresh Token이 새 토큰으로 교체된다")
    void replaceRefreshTokenOnReLoginSameDevice() {
        // given: 이메일 인증된 사용자
        String email = "user@example.com";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name("테스트유저")
                .email(email)
                .password(encodedPassword)
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId = "test-device-005";
        LoginUser.Command command = new LoginUser.Command(email, rawPassword, deviceId);

        // when: 첫 번째 로그인
        LoginUser.Result firstLogin = loginUser.execute(command);

        // when: 같은 기기에서 두 번째 로그인
        LoginUser.Result secondLogin = loginUser.execute(command);

        // then: 두 번째 로그인이 성공적으로 완료됨
        assertThat(secondLogin.userId()).isEqualTo(savedUser.getId());
        assertThat(secondLogin.accessToken()).isNotNull().isNotEmpty();
        assertThat(secondLogin.refreshToken()).isNotNull().isNotEmpty();

        // then: DB에는 해당 기기의 최신 Refresh Token만 존재
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findByUserIdAndDeviceId(savedUser.getId(), deviceId);
        assertThat(savedRefreshToken).isPresent();
        assertThat(savedRefreshToken.get().getToken()).isEqualTo(secondLogin.refreshToken());
    }

    @Test
    @DisplayName("동일 사용자가 다른 기기에서 로그인하면 각 기기별로 Refresh Token이 독립적으로 저장된다")
    void multiDeviceLogin() {
        // given: 이메일 인증된 사용자
        String email = "user@example.com";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .name("테스트유저")
                .email(email)
                .password(encodedPassword)
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId1 = "device-mobile";
        String deviceId2 = "device-desktop";

        // when: 첫 번째 기기에서 로그인
        LoginUser.Result loginDevice1 = loginUser.execute(new LoginUser.Command(email, rawPassword, deviceId1));

        // when: 두 번째 기기에서 로그인
        LoginUser.Result loginDevice2 = loginUser.execute(new LoginUser.Command(email, rawPassword, deviceId2));

        // then: 각 기기별로 독립적인 Refresh Token 저장됨
        Optional<RefreshToken> tokenDevice1 = refreshTokenRepository.findByUserIdAndDeviceId(savedUser.getId(), deviceId1);
        Optional<RefreshToken> tokenDevice2 = refreshTokenRepository.findByUserIdAndDeviceId(savedUser.getId(), deviceId2);

        assertThat(tokenDevice1).isPresent();
        assertThat(tokenDevice2).isPresent();
        assertThat(tokenDevice1.get().getToken()).isEqualTo(loginDevice1.refreshToken());
        assertThat(tokenDevice2.get().getToken()).isEqualTo(loginDevice2.refreshToken());
        assertThat(tokenDevice1.get().getToken()).isNotEqualTo(tokenDevice2.get().getToken());
    }
}
