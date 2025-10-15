package app.aipacemaker.backend.auth.infrastructure;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JWT 토큰 생성 및 검증 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        // 테스트용 고정 설정값
        String secret = "test-secret-key-for-unit-testing-must-be-at-least-256-bits-long-to-satisfy-hmac-sha-256";
        long accessTokenExpiry = 3600000L;  // 1시간
        long refreshTokenExpiry = 2592000000L; // 30일

        jwtTokenProvider = new JwtTokenProvider(secret, accessTokenExpiry, refreshTokenExpiry);
    }

    @Test
    @DisplayName("Access Token을 생성하면 유효한 JWT 토큰이 반환된다")
    void generateAccessToken() {
        // given
        Long userId = 1L;
        String email = "test@example.com";

        // when
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT는 header.payload.signature 구조
    }

    @Test
    @DisplayName("Refresh Token을 생성하면 유효한 JWT 토큰이 반환된다")
    void generateRefreshToken() {
        // given
        Long userId = 1L;

        // when
        String token = jwtTokenProvider.generateRefreshToken(userId);

        // then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("유효한 Access Token에서 userId를 추출하면 원본 userId가 반환된다")
    void extractUserIdFromAccessToken() {
        // given
        Long userId = 123L;
        String email = "user@example.com";
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // when
        Long extractedUserId = jwtTokenProvider.extractUserId(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("유효한 Access Token에서 email을 추출하면 원본 email이 반환된다")
    void extractEmailFromAccessToken() {
        // given
        Long userId = 123L;
        String email = "user@example.com";
        String token = jwtTokenProvider.generateAccessToken(userId, email);

        // when
        String extractedEmail = jwtTokenProvider.extractEmail(token);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("유효한 토큰을 검증하면 true를 반환한다")
    void validateValidToken() {
        // given
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com");

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰을 검증하면 false를 반환한다")
    void validateExpiredToken() {
        // given: 즉시 만료되는 토큰 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "test-secret-key-for-unit-testing-must-be-at-least-256-bits-long-to-satisfy-hmac-sha-256",
                -1000L,  // 음수로 설정하여 즉시 만료
                1000L
        );
        String token = shortLivedProvider.generateAccessToken(1L, "test@example.com");

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰을 검증하면 false를 반환한다")
    void validateMalformedToken() {
        // given
        String malformedToken = "invalid.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 서명의 토큰을 검증하면 false를 반환한다")
    void validateTokenWithWrongSignature() {
        // given: 다른 secret으로 생성된 토큰
        JwtTokenProvider differentProvider = new JwtTokenProvider(
                "different-secret-key-for-testing-must-be-at-least-256-bits-long-to-satisfy-hmac-sha",
                3600000L,
                2592000000L
        );
        String token = differentProvider.generateAccessToken(1L, "test@example.com");

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Access Token의 만료 시간을 추출하면 발급 시간으로부터 1시간 후이다")
    void getExpirationTimeOfAccessToken() {
        // given
        LocalDateTime beforeCreation = LocalDateTime.now();
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com");
        LocalDateTime afterCreation = LocalDateTime.now();

        // when
        LocalDateTime expirationTime = jwtTokenProvider.getExpirationTime(token);

        // then: 발급 시간 + 1시간 범위 내에 있어야 함
        LocalDateTime expectedMin = beforeCreation.plusHours(1).minusSeconds(1);
        LocalDateTime expectedMax = afterCreation.plusHours(1).plusSeconds(1);
        assertThat(expirationTime).isBetween(expectedMin, expectedMax);
    }
}
