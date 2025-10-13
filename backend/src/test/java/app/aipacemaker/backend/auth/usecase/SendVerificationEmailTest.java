package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.TestcontainersConfiguration;
import app.aipacemaker.backend.config.mail.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("SendVerificationEmail Use Case 통합 테스트")
class SendVerificationEmailTest {

    @Autowired
    private SendVerificationEmail sendVerificationEmail;

    @SpyBean
    private EmailSender emailSender;

    @Test
    @DisplayName("유효한 토큰으로 인증 이메일을 발송한다")
    void sendVerificationEmailSuccessfully() {
        // given
        String email = "test@example.com";
        String token = "test-token-123";
        SendVerificationEmail.Command command = new SendVerificationEmail.Command(email, token);

        // when
        SendVerificationEmail.Result result = sendVerificationEmail.execute(command);

        // then
        assertThat(result.emailSent()).isTrue();

        // EmailSender가 호출되었는지 검증
        verify(emailSender).sendEmail(
                anyString(),
                contains("이메일 인증"),
                contains(token)
        );
    }
}
