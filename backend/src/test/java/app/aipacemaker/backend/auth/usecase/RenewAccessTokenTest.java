package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.BaseIntegrationTest;
import app.aipacemaker.backend.auth.infrastructure.JwtTokenProvider;
import app.aipacemaker.backend.auth.model.RefreshToken;
import app.aipacemaker.backend.auth.model.RefreshTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import app.aipacemaker.backend.auth.model.exception.ExpiredRefreshTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Access Token 갱신 통합 테스트
 *
 * 테스트 범위:
 * RenewAccessToken UseCase → Refresh Token 검증 → 새로운 Access Token 발급
 */
@DisplayName("Access Token 갱신 통합 테스트")
class RenewAccessTokenTest extends BaseIntegrationTest {

    @Autowired
    private RenewAccessToken renewAccessToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("유효한 Refresh Token으로 요청하면 새로운 Access Token이 발급된다")
    void successfulRefresh() {
        // given: 사용자 및 유효한 Refresh Token 생성
        User user = User.builder()
                .name("테스트유저")
                .email("user@example.com")
                .password("encodedPassword")
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId = "test-device";
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(savedUser.getId(), deviceId);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(savedUser.getId())
                .deviceId(deviceId)
                .token(refreshTokenString)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(refreshToken);

        RenewAccessToken.Command command = new RenewAccessToken.Command(refreshTokenString);

        // when: Access Token 갱신 요청
        RenewAccessToken.Result result = renewAccessToken.execute(command);

        // then: 새로운 Access Token이 발급됨
        assertThat(result.accessToken()).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(result.accessToken())).isTrue();
        assertThat(jwtTokenProvider.extractUserId(result.accessToken())).isEqualTo(savedUser.getId());
        assertThat(jwtTokenProvider.extractEmail(result.accessToken())).isEqualTo(savedUser.getEmail());
    }

    @Test
    @DisplayName("JWT 형식이 잘못된 Refresh Token으로 요청하면 InvalidRefreshTokenException이 발생한다")
    void invalidTokenFormat() {
        // given: 잘못된 형식의 토큰
        RenewAccessToken.Command command = new RenewAccessToken.Command("invalid.token.format");

        // when & then: InvalidRefreshTokenException 발생
        assertThatThrownBy(() -> renewAccessToken.execute(command))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");
    }

    @Test
    @DisplayName("DB에 존재하지 않는 Refresh Token으로 요청하면 InvalidRefreshTokenException이 발생한다")
    void tokenNotInDatabase() {
        // given: 유효한 JWT이지만 DB에 없는 토큰
        String validButNotStoredToken = jwtTokenProvider.generateRefreshToken(999L, "unknown-device");
        RenewAccessToken.Command command = new RenewAccessToken.Command(validButNotStoredToken);

        // when & then: InvalidRefreshTokenException 발생
        assertThatThrownBy(() -> renewAccessToken.execute(command))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 요청하면 ExpiredRefreshTokenException이 발생한다")
    void expiredToken() {
        // given: 사용자 및 만료된 Refresh Token 생성
        User user = User.builder()
                .name("테스트유저")
                .email("user@example.com")
                .password("encodedPassword")
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String deviceId = "test-device";
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(savedUser.getId(), deviceId);

        RefreshToken expiredToken = RefreshToken.builder()
                .userId(savedUser.getId())
                .deviceId(deviceId)
                .token(refreshTokenString)
                .expiresAt(LocalDateTime.now().minusDays(1))  // 이미 만료됨
                .build();
        refreshTokenRepository.save(expiredToken);

        RenewAccessToken.Command command = new RenewAccessToken.Command(refreshTokenString);

        // when & then: ExpiredRefreshTokenException 발생
        assertThatThrownBy(() -> renewAccessToken.execute(command))
                .isInstanceOf(ExpiredRefreshTokenException.class)
                .hasMessageContaining("만료된 Refresh Token입니다");
    }

    @Test
    @DisplayName("다른 기기의 Refresh Token으로 요청하면 deviceId가 일치하지 않아 InvalidRefreshTokenException이 발생한다")
    void mismatchedDeviceId() {
        // given: 사용자 및 특정 기기의 Refresh Token 생성
        User user = User.builder()
                .name("테스트유저")
                .email("user@example.com")
                .password("encodedPassword")
                .emailVerified(true)
                .build();
        User savedUser = userRepository.save(user);

        String actualDeviceId = "device-A";
        String stolenDeviceId = "device-B";

        // device-A의 토큰 생성 및 저장
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(savedUser.getId(), actualDeviceId);
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(savedUser.getId())
                .deviceId(actualDeviceId)
                .token(refreshTokenString)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        refreshTokenRepository.save(refreshToken);

        // device-B의 토큰 생성 (DB에 저장 안 함, 도용 시나리오)
        String stolenToken = jwtTokenProvider.generateRefreshToken(savedUser.getId(), stolenDeviceId);
        RenewAccessToken.Command command = new RenewAccessToken.Command(stolenToken);

        // when & then: InvalidRefreshTokenException 발생
        assertThatThrownBy(() -> renewAccessToken.execute(command))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");
    }
}
