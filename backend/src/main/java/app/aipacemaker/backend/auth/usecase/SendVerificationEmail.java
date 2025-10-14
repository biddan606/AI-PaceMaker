package app.aipacemaker.backend.auth.usecase;

import app.aipacemaker.backend.config.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendVerificationEmail {

    private final EmailSender emailSender;

    public Result execute(Command command) {
        String subject = "이메일 인증을 완료해주세요";
        String body = String.format("""
                안녕하세요,

                회원가입을 환영합니다!
                아래 링크를 클릭하여 이메일 인증을 완료해주세요.

                인증 토큰: %s

                이 토큰은 24시간 동안 유효합니다.

                감사합니다.
                """, command.token);

        emailSender.sendEmail(command.email, subject, body);

        return new Result(true);
    }

    public record Command(String email, String token) {}

    public record Result(boolean emailSent) {}
}
