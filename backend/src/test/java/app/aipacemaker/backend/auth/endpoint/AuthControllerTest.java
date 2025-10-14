package app.aipacemaker.backend.auth.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.aipacemaker.backend.auth.model.exception.DuplicateEmailException;
import app.aipacemaker.backend.auth.model.exception.ExpiredVerificationTokenException;
import app.aipacemaker.backend.auth.model.exception.InvalidPasswordException;
import app.aipacemaker.backend.auth.model.exception.InvalidVerificationTokenException;
import app.aipacemaker.backend.auth.usecase.RegisterUser;
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

    @Test
    @DisplayName("POST /api/users - 유효한 요청이면 201 Created와 userId, email, message를 반환한다")
    void createUserSuccess() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        RegisterUser.Result result = new RegisterUser.Result(1L, "test@example.com", true);
        when(registerUser.execute(any(RegisterUser.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users - 필수 필드 누락 시 400 Bad Request를 반환한다")
    void missingFieldsReturns400() throws Exception {
        // given: password 필드 누락
        String invalidRequest = "{\"email\":\"test@example.com\"}";

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
        RegisterRequest request = new RegisterRequest("invalid-email", "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/verification - 유효한 토큰이면 200 OK와 userId, email, verified, message를 반환한다")
    void verifyEmailSuccess() throws Exception {
        // given
        VerifyEmailRequest request = new VerifyEmailRequest("valid-token");
        VerifyEmail.Result result = new VerifyEmail.Result(1L, "test@example.com", true);
        when(verifyEmail.execute(any(VerifyEmail.Command.class))).thenReturn(result);

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users - 비밀번호 검증 실패 시 400 Bad Request와 ProblemDetail을 반환한다")
    void invalidPasswordReturns400() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "short1");
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
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123");
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
        VerifyEmailRequest request = new VerifyEmailRequest("expired-token");
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
        VerifyEmailRequest request = new VerifyEmailRequest("nonexistent-token");
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

    record RegisterRequest(String email, String password) {

    }

    record VerifyEmailRequest(String token) {

    }
}
