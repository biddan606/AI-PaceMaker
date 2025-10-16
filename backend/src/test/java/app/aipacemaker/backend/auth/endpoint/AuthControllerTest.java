package app.aipacemaker.backend.auth.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.aipacemaker.backend.auth.model.exception.*;
import app.aipacemaker.backend.auth.usecase.LoginUser;
import app.aipacemaker.backend.auth.usecase.RegisterUser;
import app.aipacemaker.backend.auth.usecase.RenewAccessToken;
import app.aipacemaker.backend.auth.usecase.VerifyEmail;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * AuthController API 테스트
 *
 * HTTP 레이어만 검증: 요청/응답 직렬화, 상태 코드, 예외 핸들링
 * UseCase는 MockitoBean으로 격리하여 HTTP 계층만 테스트
 * Security는 제외하여 테스트 단순화 (Security 통합은 통합 테스트에서 검증)
 * 비즈니스 로직은 통합 테스트에서 검증
 */
@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@DisplayName("AuthController API 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterUser registerUser;

    @MockitoBean
    private VerifyEmail verifyEmail;

    @MockitoBean
    private LoginUser loginUser;

    @MockitoBean
    private RenewAccessToken renewAccessToken;

    @Test
    @DisplayName("POST /api/users - 유효한 요청이면 201 Created와 userId, email을 반환한다")
    void createUserSuccess() throws Exception {
        // given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest("홍길동", "test@example.com", "password123");
        RegisterUser.Result result = new RegisterUser.Result(1L, "test@example.com", true);
        when(registerUser.execute(any(RegisterUser.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("POST /api/users - 필수 필드 누락 시 400 Bad Request를 반환한다")
    void missingFieldsReturns400() throws Exception {
        // given: name 필드 누락
        String invalidRequest = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - 잘못된 이메일 형식이면 400 Bad Request를 반환한다")
    void invalidEmailFormatReturns400() throws Exception {
        // given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest("테스터", "invalid-email", "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/verification - 유효한 토큰이면 200 OK와 userId, email, verified를 반환한다")
    void verifyEmailSuccess() throws Exception {
        // given
        AuthController.VerifyEmailRequest request = new AuthController.VerifyEmailRequest("valid-token");
        VerifyEmail.Result result = new VerifyEmail.Result(1L, "test@example.com", true);
        when(verifyEmail.execute(any(VerifyEmail.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @DisplayName("POST /api/users - 비밀번호 검증 실패 시 400 Bad Request와 ProblemDetail을 반환한다")
    void invalidPasswordReturns400() throws Exception {
        // given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest("테스터", "test@example.com", "short1");
        when(registerUser.execute(any(RegisterUser.Command.class)))
                .thenThrow(new InvalidPasswordException("비밀번호는 8자 이상이며 영문자와 숫자를 포함해야 합니다."));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/users - 중복 이메일이면 409 Conflict와 ProblemDetail을 반환한다")
    void duplicateEmailReturns409() throws Exception {
        // given
        AuthController.RegisterRequest request = new AuthController.RegisterRequest("기존유저", "existing@example.com", "password123");
        when(registerUser.execute(any(RegisterUser.Command.class)))
                .thenThrow(new DuplicateEmailException("existing@example.com"));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/users/verification - 만료된 토큰이면 400 Bad Request와 ProblemDetail을 반환한다")
    void expiredTokenReturns400() throws Exception {
        // given
        AuthController.VerifyEmailRequest request = new AuthController.VerifyEmailRequest("expired-token");
        when(verifyEmail.execute(any(VerifyEmail.Command.class)))
                .thenThrow(new ExpiredVerificationTokenException("expired-token"));

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/users/verification - 존재하지 않는 토큰이면 404 Not Found와 ProblemDetail을 반환한다")
    void invalidTokenReturns404() throws Exception {
        // given
        AuthController.VerifyEmailRequest request = new AuthController.VerifyEmailRequest("nonexistent-token");
        when(verifyEmail.execute(any(VerifyEmail.Command.class)))
                .thenThrow(new InvalidVerificationTokenException("nonexistent-token"));

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/auth/login - 유효한 이메일과 비밀번호로 로그인하면 200 OK와 사용자 정보 및 쿠키를 반환한다")
    void loginSuccess() throws Exception {
        // given
        AuthController.LoginRequest request = new AuthController.LoginRequest("test@example.com", "password123", "device-001");
        LoginUser.Result result = new LoginUser.Result(
                1L,
                "test@example.com",
                true,
                "access-token",
                "refresh-token"
        );
        when(loginUser.execute(any(LoginUser.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(true))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("POST /api/auth/login - 필수 필드 누락 시 400 Bad Request를 반환한다")
    void loginMissingFieldsReturns400() throws Exception {
        // given: deviceId 필드 누락
        String invalidRequest = "{\"email\":\"test@example.com\",\"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 잘못된 이메일 형식이면 400 Bad Request를 반환한다")
    void loginInvalidEmailFormatReturns400() throws Exception {
        // given
        AuthController.LoginRequest request = new AuthController.LoginRequest("invalid-email", "password123", "device-001");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - 잘못된 인증 정보면 401 Unauthorized와 ProblemDetail을 반환한다")
    void loginInvalidCredentialsReturns401() throws Exception {
        // given
        AuthController.LoginRequest request = new AuthController.LoginRequest("test@example.com", "wrongpassword", "device-001");
        when(loginUser.execute(any(LoginUser.Command.class)))
                .thenThrow(new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 유효한 Refresh Token이면 200 OK와 새로운 Access Token을 반환한다")
    void refreshTokenSuccess() throws Exception {
        // given
        AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest("valid-refresh-token");
        RenewAccessToken.Result result = new RenewAccessToken.Result("new-access-token");
        when(renewAccessToken.execute(any(RenewAccessToken.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 필수 필드 누락 시 400 Bad Request를 반환한다")
    void refreshTokenMissingFieldReturns400() throws Exception {
        // given: refreshToken 필드 누락
        String invalidRequest = "{}";

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 유효하지 않은 Refresh Token이면 401 Unauthorized와 ProblemDetail을 반환한다")
    void refreshTokenInvalidTokenReturns401() throws Exception {
        // given
        AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest("invalid-token");
        when(renewAccessToken.execute(any(RenewAccessToken.Command.class)))
                .thenThrow(new InvalidRefreshTokenException("유효하지 않은 Refresh Token입니다."));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 만료된 Refresh Token이면 401 Unauthorized와 ProblemDetail을 반환한다")
    void refreshTokenExpiredTokenReturns401() throws Exception {
        // given
        AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest("expired-token");
        when(renewAccessToken.execute(any(RenewAccessToken.Command.class)))
                .thenThrow(new ExpiredRefreshTokenException("만료된 Refresh Token입니다."));

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(401));
    }
}
