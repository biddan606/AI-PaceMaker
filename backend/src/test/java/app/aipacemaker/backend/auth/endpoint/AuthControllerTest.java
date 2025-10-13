package app.aipacemaker.backend.auth.endpoint;

import app.aipacemaker.backend.TestcontainersConfiguration;
import app.aipacemaker.backend.auth.model.EmailVerificationToken;
import app.aipacemaker.backend.auth.model.EmailVerificationTokenRepository;
import app.aipacemaker.backend.auth.model.User;
import app.aipacemaker.backend.auth.model.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Endpoint 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Test
    @DisplayName("POST /api/users - 회원가입 성공")
    void registerUserSuccessfully() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users - 비밀번호 정책 위반 시 400")
    void registerUserWithInvalidPassword() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "short1");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - 중복 이메일 시 409")
    void registerUserWithDuplicateEmail() throws Exception {
        // given
        User existingUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword123")
                .emailVerified(false)
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/users - 필수 필드 누락 시 400")
    void registerUserWithMissingFields() throws Exception {
        // given
        String invalidRequest = "{\"email\":\"test@example.com\"}";

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users - 잘못된 이메일 형식 시 400")
    void registerUserWithInvalidEmailFormat() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("invalid-email", "password123");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/verification - 이메일 인증 성공")
    void verifyEmailSuccessfully() throws Exception {
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

        VerifyEmailRequest request = new VerifyEmailRequest(token);

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/users/verification - 만료된 토큰 시 400")
    void verifyEmailWithExpiredToken() throws Exception {
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

        VerifyEmailRequest request = new VerifyEmailRequest(token);

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/verification - 존재하지 않는 토큰 시 404")
    void verifyEmailWithInvalidToken() throws Exception {
        // given
        VerifyEmailRequest request = new VerifyEmailRequest("nonexistent-token");

        // when & then
        mockMvc.perform(post("/api/users/verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    record RegisterRequest(String email, String password) {}

    record VerifyEmailRequest(String token) {}
}
